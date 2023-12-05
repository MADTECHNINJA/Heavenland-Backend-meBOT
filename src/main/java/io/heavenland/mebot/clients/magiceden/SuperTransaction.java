package io.heavenland.mebot.clients.magiceden;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.*;
import org.p2p.solanaj.utils.ShortvecEncoding;
import org.p2p.solanaj.utils.TweetNaclFast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuperTransaction {

    public static final int SIGNATURE_LENGTH = 64;

    private SuperMessage message;
    private List<String> signatures;
    private byte[] serializedMessage;
    private PublicKey feePayer;

    public SuperTransaction() {
        this.message = new SuperMessage();
        this.signatures = new ArrayList<String>();
    }

    public SuperTransaction addInstruction(TransactionInstruction instruction) {
        message.addInstruction(instruction);
        return this;
    }

    public void setRecentBlockHash(String recentBlockhash) {
        message.setRecentBlockHash(recentBlockhash);
    }

    public void setFeePayer(PublicKey feePayer) {
        this.feePayer = feePayer;
    }

    private void setFeePayer(PublicKey feePayer, boolean setForMessage) {
        this.feePayer = feePayer;
        if (setForMessage) {
            this.message.setFeePayer(feePayer);
        }
    }

    public void sign(Account signer) {
        sign(signer, false);
    }
    public void sign(Account signer, boolean replaceInsteadOfAdd) {
        sign(Arrays.asList(signer), replaceInsteadOfAdd);
    }

    public void sign(List<Account> signers) {
        sign(signers, false);
    }

    public void sign(List<Account> signers, boolean replaceInsteadOfAdd) {

        if (signers.size() == 0) {
            throw new IllegalArgumentException("No signers");
        }

        if (feePayer == null) {
            feePayer = signers.get(0).getPublicKey();
        }
        message.setFeePayer(feePayer);

        serializedMessage = message.serialize();

        var i = 0;
        for (Account signer : signers) {
            TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
            byte[] signature = signatureProvider.detached(serializedMessage);

            if (!replaceInsteadOfAdd) {
                signatures.add(Base58.encode(signature));
            }
            else {
                signatures.set(i++, Base58.encode(signature));
            }
        }
    }

    private void sign(String signature) {
        signatures.add(signature);
    }

    public byte[] serialize() {
        int signaturesSize = signatures.size();
        byte[] signaturesLength = ShortvecEncoding.encodeLength(signaturesSize);

        ByteBuffer out = ByteBuffer
                .allocate(signaturesLength.length + signaturesSize * SIGNATURE_LENGTH + serializedMessage.length);

        out.put(signaturesLength);

        for (String signature : signatures) {
            byte[] rawSignature = Base58.decode(signature);
            out.put(rawSignature);
        }

        out.put(serializedMessage);

        return out.array();
    }

    public String getSignature() {
        if (signatures.size() > 0) {
            return signatures.get(0);
        }

        return null;
    }

    //

    public static byte[] decodeLengthEater(byte[] byteArray) {
        do {
            byteArray = Arrays.copyOfRange(byteArray, 1, byteArray.length);
        } while (byteArray[0] > 0 && (byteArray[0] & 128) != 0);
        return byteArray;
    }

    private static int byteToUByte(byte value) {
        return value >= 0 ? value : value + 256;
    }

    public static SuperTransaction from(byte[] buffer) {
        // inspired by https://github.com/solana-labs/solana-web3.js/blob/0b3de2b72dca5524750a60381dbb50b8d4405149/src/transaction.ts#L789

        var byteArray = buffer;

        var signatureCount = ShortvecEncoding.decodeLength(byteArray);
        byteArray = decodeLengthEater(byteArray);

        List<String> signatures = new ArrayList<>();
        for (int i = 0; i < signatureCount; i++) {
            var signature = Arrays.copyOfRange(byteArray, 0, SIGNATURE_LENGTH);
            byteArray = Arrays.copyOfRange(byteArray, SIGNATURE_LENGTH, byteArray.length);
            signatures.add(Base58.encode(signature));
        }

        return populate(SuperMessage.from(byteArray, signatureCount), signatures);
    }

    protected static SuperTransaction populate(SuperMessage message, List<String> signatures) {

        var transaction = new SuperTransaction();

        transaction.setRecentBlockHash(message.getRecentBlockhash());

        message.getDecodedInstructions().forEach(transaction::addInstruction);

        signatures.forEach(transaction::sign);

        if (message.getMessageHeader().getNumRequiredSignatures() > 0) {
            transaction.setFeePayer(message.getFeePayer(), true);
        }

        transaction.serializedMessage = transaction.message.serialize();

        return transaction;
    }
}
