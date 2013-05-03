/*
 * Copyright 2013 Peergreen S.A.S.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.shelbie.security.internal;

import static java.lang.String.format;

import java.util.Collection;
import java.util.Set;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.service.command.CommandSession;
import org.fusesource.jansi.Ansi;

import com.peergreen.security.realm.AccountFilter;
import com.peergreen.security.realm.AccountInfo;
import com.peergreen.security.realm.AccountStore;
import com.peergreen.security.realm.ModifiableAccountStore;
import com.peergreen.security.realm.manager.AccountStoreManager;
import com.peergreen.security.realm.manager.ServiceHandle;

/**
 * User: guillaume
 * Date: 15/03/13
 * Time: 22:03
 */
@Component
@Command(name = "list-accounts",
        scope = "security",
        description = "List accounts from the given account store.")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class ListAccountsAction implements Action {

    private AccountStoreManager manager;

    @Option(name = "-s",
            aliases = "--store",
            description = "Name of the store where account will be searched")
    private String storeName;

    public ListAccountsAction(@Requires(proxy = false, nullable = false) AccountStoreManager manager) {
        this.manager = manager;
    }

    @Override
    public Object execute(CommandSession session) throws Exception {

        if (storeName == null) {
            storeName = (String) session.get(Constants.STORE_NAME_VARIABLE);
        }
        if (storeName == null) {
            throw new Exception("Store name is missing (provide it as argument or using set-session-realm)");
        }

        ServiceHandle<AccountStore> handle = manager.findAccountStore(storeName, AccountStore.class);
        if (handle == null) {
            throw new Exception(
                    format("Cannot find AccountStore named '%s'", storeName)
            );
        }

        try {
            AccountStore store = handle.get();

            Set<AccountInfo> accounts = store.getAccounts(new AccountFilter() {
                @Override
                public boolean accept(AccountInfo account) {
                    return true;
                }
            });

            if (accounts.isEmpty()) {
                System.out.printf("AccountStore '%s' does not contains any account.", storeName);
                return null;
            }

            System.out.printf("AccountStore '%s' contains %d account(s).%n", storeName, accounts.size());
            Ansi buffer = Ansi.ansi();
            buffer.render("[%8s] %25s %s%n", "Status", "Login", "Roles");
            for (AccountInfo account : accounts) {
                buffer.render(format("[@|%s %8s|@] %25s %s%n",
                              asColor(account.isActivated()),
                              asText(account.isActivated()),
                              account.getLogin(),
                              asText(account.getRoles(), 60)));
            }

            System.out.print(buffer.toString());

            return null;
        } finally {
            handle.release();
        }
    }

    private String asColor(boolean activated) {
        if (activated) {
            return "green";
        } else {
            return "red";
        }
    }

    private String asText(Collection<String> values, int max) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(value);
        }

        if (sb.length() > max) {
            return format("%d roles", values.size());
        }

        return sb.toString();
    }

    private String asText(boolean activated) {
        if (activated) {
            return "enabled";
        } else {
            return "disabled";
        }
    }
}
