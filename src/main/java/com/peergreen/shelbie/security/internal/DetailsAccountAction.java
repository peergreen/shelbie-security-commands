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
@Command(name = "detail-account",
        scope = "security",
        description = "Details an account from the given account store.")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class DetailsAccountAction implements Action {

    private AccountStoreManager manager;

    @Option(name = "-s",
            aliases = "--store",
            description = "Name of the store where account will be searched")
    private String storeName;

    @Argument(name = "login",
              required = true,
              description = "Login of the searched account")
    private String login;

    public DetailsAccountAction(@Requires AccountStoreManager manager) {
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

        AccountStore store = handle.get();

        try {
            Set<AccountInfo> accounts = store.getAccounts(new AccountFilter() {
                @Override
                public boolean accept(AccountInfo account) {
                    return login.equals(account.getLogin());
                }
            });

            if (accounts.isEmpty()) {
                System.out.printf("AccountStore '%s' does not contains account '%s'.", storeName, login);
                return null;
            }

            AccountInfo account = accounts.iterator().next();

            Ansi buffer = Ansi.ansi();
            buffer.render("Login: %s (%s)%n", login, asText(account.isActivated()));
            buffer.render("Roles (%d):%n", account.getRoles().size());
            for (String role : account.getRoles()) {
                buffer.render("  * %s%n", role);
            }

            System.out.print(buffer.toString());

            return null;
        } finally {
            handle.release();
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
