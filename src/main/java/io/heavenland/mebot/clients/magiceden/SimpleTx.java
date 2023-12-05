package io.heavenland.mebot.clients.magiceden;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.Message;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.utils.ShortvecEncoding;
import org.p2p.solanaj.utils.TweetNaclFast;

import java.nio.ByteBuffer;
import java.util.*;

@Deprecated
// not working
public class SimpleTx extends Transaction {

	private final List<String> signatures = new ArrayList<>();
	private final byte[] serializedMessage;
	private PublicKey feePayer;

	public SimpleTx(byte[] serializedMessage) {
		this.serializedMessage = serializedMessage;
	}

	@Override
	public void sign(Account signer) {
		sign(Collections.singletonList(signer));
	}

	@Override
	public void sign(List<Account> signers) {
		if (signers.size() == 0) {
			throw new IllegalArgumentException("No signers");
		} else {
			if (this.feePayer == null) {
				this.feePayer = (signers.get(0)).getPublicKey();
			}
			for (Account signer : signers) {
				TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
				byte[] signature = signatureProvider.detached(this.serializedMessage);
				this.signatures.add(Base58.encode(signature));
			}
		}
	}

	@Override
	public byte[] serialize() {
		int signaturesSize = this.signatures.size();
		byte[] signaturesLength = ShortvecEncoding.encodeLength(signaturesSize);
		ByteBuffer out = ByteBuffer.allocate(signaturesLength.length + signaturesSize * 64 + this.serializedMessage.length);
		out.put(signaturesLength);
		Iterator var4 = this.signatures.iterator();

		while(var4.hasNext()) {
			String signature = (String)var4.next();
			byte[] rawSignature = Base58.decode(signature);
			out.put(rawSignature);
		}

		out.put(this.serializedMessage);
		return out.array();
	}

	@Override
	public String getSignature() {
		return this.signatures.size() > 0 ? this.signatures.get(0) : null;
	}
}
