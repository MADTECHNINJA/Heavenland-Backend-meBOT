package io.heavenland.mebot.clients.magiceden;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.*;
import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuperMessage {

    private List<TransactionInstruction> decodedInstructions;

    public static final int PUBKEY_LENGTH = 32;

    public static class MessageHeader {
        static final int HEADER_LENGTH = 3;

        byte numRequiredSignatures = 0;
        byte numReadonlySignedAccounts = 0;
        byte numReadonlyUnsignedAccounts = 0;

        byte[] toByteArray() {
            return new byte[] { numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts };
        }

        public byte getNumRequiredSignatures() {
            return numRequiredSignatures;
        }

        public byte getNumReadonlySignedAccounts() {
            return numReadonlySignedAccounts;
        }

        public byte getNumReadonlyUnsignedAccounts() {
            return numReadonlyUnsignedAccounts;
        }
    }

    private static class CompiledInstruction {
        byte programIdIndex;
        byte[] keyIndicesCount;
        byte[] keyIndices;
        byte[] dataLength;
        byte[] data;

        int getLength() {
            // 1 = programIdIndex length
            return 1 + keyIndicesCount.length + keyIndices.length + dataLength.length + data.length;
        }
    }

    private static final int RECENT_BLOCK_HASH_LENGTH = 32;

    private SuperMessage.MessageHeader messageHeader;
    private String recentBlockhash;
    private SuperAccountKeyList accountKeys;
    private List<TransactionInstruction> instructions;
    private PublicKey feePayer;
    private List<String> programIds;

    public SuperMessage() {
        this.programIds = new ArrayList<String>();
        this.accountKeys = new SuperAccountKeyList();
        this.instructions = new ArrayList<TransactionInstruction>();
    }

    public SuperMessage addInstruction(TransactionInstruction instruction) {
        accountKeys.addAll(instruction.getKeys());
        instructions.add(instruction);

        if (!programIds.contains(instruction.getProgramId().toBase58())) {
            programIds.add(instruction.getProgramId().toBase58());
        }

        return this;
    }

    public void setRecentBlockHash(String recentBlockhash) {
        this.recentBlockhash = recentBlockhash;
    }

    public byte[] serialize() {

        if (recentBlockhash == null) {
            throw new IllegalArgumentException("recentBlockhash required");
        }

        if (instructions.size() == 0) {
            throw new IllegalArgumentException("No instructions provided");
        }

        messageHeader = new SuperMessage.MessageHeader();

        for (String programId : programIds) {
            accountKeys.add(new AccountMeta(new PublicKey(programId), false, false));
        }
        List<AccountMeta> keysList = getAccountKeys();
        int accountKeysSize = keysList.size();

        byte[] accountAddressesLength = ShortvecEncoding.encodeLength(accountKeysSize);

        int compiledInstructionsLength = 0;
        List<SuperMessage.CompiledInstruction> compiledInstructions = new ArrayList<SuperMessage.CompiledInstruction>();

        for (TransactionInstruction instruction : instructions) {
            int keysSize = instruction.getKeys().size();

            byte[] keyIndices = new byte[keysSize];
            for (int i = 0; i < keysSize; i++) {
                keyIndices[i] = (byte) AccountMeta.findAccountIndex(keysList,
                        instruction.getKeys().get(i).getPublicKey());
            }

            SuperMessage.CompiledInstruction compiledInstruction = new SuperMessage.CompiledInstruction();
            compiledInstruction.programIdIndex = (byte) AccountMeta.findAccountIndex(keysList,
                    instruction.getProgramId());
            compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(keysSize);
            compiledInstruction.keyIndices = keyIndices;
            compiledInstruction.dataLength = ShortvecEncoding.encodeLength(instruction.getData().length);
            compiledInstruction.data = instruction.getData();

            compiledInstructions.add(compiledInstruction);

            compiledInstructionsLength += compiledInstruction.getLength();
        }

        byte[] instructionsLength = ShortvecEncoding.encodeLength(compiledInstructions.size());

        int bufferSize = SuperMessage.MessageHeader.HEADER_LENGTH + RECENT_BLOCK_HASH_LENGTH + accountAddressesLength.length
                + (accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH) + instructionsLength.length
                + compiledInstructionsLength;

        ByteBuffer out = ByteBuffer.allocate(bufferSize);

        ByteBuffer accountKeysBuff = ByteBuffer.allocate(accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH);
        for (AccountMeta accountMeta : keysList) {
            accountKeysBuff.put(accountMeta.getPublicKey().toByteArray());

            if (accountMeta.isSigner()) {
                messageHeader.numRequiredSignatures += 1;
                if (!accountMeta.isWritable()) {
                    messageHeader.numReadonlySignedAccounts += 1;
                }
            } else {
                if (!accountMeta.isWritable()) {
                    messageHeader.numReadonlyUnsignedAccounts += 1;
                }
            }
        }

        out.put(messageHeader.toByteArray());

        out.put(accountAddressesLength);
        out.put(accountKeysBuff.array());

        out.put(Base58.decode(recentBlockhash));

        out.put(instructionsLength);
        for (SuperMessage.CompiledInstruction compiledInstruction : compiledInstructions) {
            out.put(compiledInstruction.programIdIndex);
            out.put(compiledInstruction.keyIndicesCount);
            out.put(compiledInstruction.keyIndices);
            out.put(compiledInstruction.dataLength);
            out.put(compiledInstruction.data);
        }

        return out.array();
    }

    protected void setFeePayer(PublicKey feePayer) {
        this.feePayer = feePayer;
    }

    public List<AccountMeta> getAccountKeys() {
        List<AccountMeta> keysList = accountKeys.getList();
        int feePayerIndex = AccountMeta.findAccountIndex(keysList, feePayer);

        List<AccountMeta> newList = new ArrayList<AccountMeta>();

        if (feePayerIndex != -1) {
            AccountMeta feePayerMeta = keysList.get(feePayerIndex);
            newList.add(new AccountMeta(feePayerMeta.getPublicKey(), true, true));
            keysList.remove(feePayerIndex);
        } else {
            newList.add(new AccountMeta(feePayer, true, true));
        }
        newList.addAll(keysList);

        return newList;
    }

    public String getRecentBlockhash() {
        return recentBlockhash;
    }

    public PublicKey getFeePayer() {
        return feePayer;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public List<TransactionInstruction> getDecodedInstructions() {
        return decodedInstructions;
    }

    public static SuperMessage from(byte[] buffer, int signatureCount) {

        //

        PublicKey feePayer = null;

        //

        var byteArray = buffer;

        int cursor = 0;

        byte numRequiredSignatures = byteArray[cursor++];
        byte numReadonlySignedAccounts = byteArray[cursor++];
        byte numReadonlyUnsignedAccounts = byteArray[cursor++];

        byteArray = Arrays.copyOfRange(byteArray, cursor, byteArray.length);

        int accountCount = ShortvecEncoding.decodeLength(byteArray);
        byteArray = SuperTransaction.decodeLengthEater(byteArray);

        var accountKeys = new ArrayList<String>();
        for (int i = 0; i < accountCount; i++) {
            var account = Arrays.copyOfRange(byteArray, 0, PUBKEY_LENGTH);
            byteArray = Arrays.copyOfRange(byteArray, PUBKEY_LENGTH, byteArray.length);
            accountKeys.add(Base58.encode(account));
        }

        var recentBlockhash = Arrays.copyOfRange(byteArray, 0, PUBKEY_LENGTH);
        byteArray = Arrays.copyOfRange(byteArray, PUBKEY_LENGTH, byteArray.length);

        int instructionCount = ShortvecEncoding.decodeLength(byteArray);
        byteArray = SuperTransaction.decodeLengthEater(byteArray);
        var instructions = new ArrayList<CompiledInstruction>();
        var realInstructions = new ArrayList<TransactionInstruction>();
        for (int i = 0; i < instructionCount; i++) {
            byte programIdIndex = byteArray[0];
            byteArray = Arrays.copyOfRange(byteArray, 1, byteArray.length);

            int accountCount2 = ShortvecEncoding.decodeLength(byteArray);
            byteArray = SuperTransaction.decodeLengthEater(byteArray);
            byte[] accounts = Arrays.copyOfRange(byteArray, 0, accountCount2);
            byteArray = Arrays.copyOfRange(byteArray, accountCount2, byteArray.length);

            int dataLength = ShortvecEncoding.decodeLength(byteArray);
            byteArray = SuperTransaction.decodeLengthEater(byteArray);
            byte[] dataSlice = Arrays.copyOfRange(byteArray, 0, dataLength);

            if (byteArray.length > dataLength) {
                byteArray = Arrays.copyOfRange(byteArray, dataLength, byteArray.length);
            }

            String data = Base58.encode(dataSlice);

            var compiledInstruction = new SuperMessage.CompiledInstruction();
            compiledInstruction.programIdIndex = programIdIndex;
            compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(accountCount2);
            compiledInstruction.keyIndices = accounts;
            compiledInstruction.dataLength = ShortvecEncoding.encodeLength(dataLength);
            compiledInstruction.data = dataSlice;

            instructions.add(compiledInstruction);

            //

            var accountMetas = new ArrayList<AccountMeta>();
            for (int index = 0; index < accountCount2; index++) {
                var accountMeta = new AccountMeta(
                        new PublicKey(accountKeys.get(accounts[index])),
                        isAccountSigner(accounts[index], numRequiredSignatures),
                        isAccountWritable(accounts[index], accountCount, numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts)
                );
                accountMetas.add(accountMeta);
            }

            realInstructions.add(
                    new TransactionInstruction(
                            new PublicKey(accountKeys.get(programIdIndex)),
                            accountMetas,
                            dataSlice
                    )
            );

            if (feePayer == null) {
                feePayer = accountMetas.get(0).getPublicKey();
            }
        }

        var m = new SuperMessage();

        m.setRecentBlockHash(Base58.encode(recentBlockhash));
        m.decodedInstructions = realInstructions;
        realInstructions.forEach(m::addInstruction);
        m.setFeePayer(feePayer);
        m.serialize();

        return m;
    }

    public static boolean isAccountSigner(int index, int numRequiredSignatures) {
        return index < numRequiredSignatures;
    }

    public static boolean isAccountWritable(int index, int accountKeysLength, int numRequiredSignatures, int numReadonlySignedAccounts, int numReadonlyUnsignedAccounts) {
        return (
                index <
                        numRequiredSignatures -
                                numReadonlySignedAccounts ||
                        (index >= numRequiredSignatures &&
                                index <
                                        accountKeysLength - numReadonlyUnsignedAccounts)
        );
    }
}
