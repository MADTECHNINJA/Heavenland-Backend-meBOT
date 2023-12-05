package io.heavenland.mebot.clients.magiceden;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;

import java.util.*;

public class SuperAccountKeyList {
    private List<AccountMeta> accountsList = new ArrayList();
    private static final Comparator<AccountMeta> metaComparator = new Comparator<AccountMeta>() {
        public int compare(AccountMeta am1, AccountMeta am2) {
            int cmpSigner = am1.isSigner() == am2.isSigner() ? 0 : (am1.isSigner() ? -1 : 1);
            if (cmpSigner != 0) {
                return cmpSigner;
            } else {
                int cmpkWritable = am1.isWritable() == am2.isWritable() ? 0 : (am1.isWritable() ? -1 : 1);
                return cmpkWritable != 0 ? cmpkWritable : 0;
            }
        }
    };

    public SuperAccountKeyList() {
    }

    public void add(AccountMeta accountMeta) {
        this.accountsList.add(accountMeta);
    }

    public void addAll(Collection<AccountMeta> metas) {
        this.accountsList.addAll(metas);
    }

    public List<AccountMeta> getList() {
        ArrayList<AccountMeta> uniqueMetas = new ArrayList();
        Iterator var2 = this.accountsList.iterator();

        while(true) {
            while(var2.hasNext()) {
                AccountMeta accountMeta = (AccountMeta)var2.next();
                PublicKey pubKey = accountMeta.getPublicKey();
                int index = AccountMeta.findAccountIndex(uniqueMetas, pubKey);
                if (index > -1) {
                    uniqueMetas.set(index, accountMeta);  // this line is different from the AccountKeyList class
                } else {
                    uniqueMetas.add(accountMeta);
                }
            }

            uniqueMetas.sort(metaComparator);
            return uniqueMetas;
        }
    }
}
