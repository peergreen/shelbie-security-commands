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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.service.command.CommandSession;

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
@Command(name = "create-account",
        scope = "security",
        description = "Creates a new account in the given account store.")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class CreateAccountAction implements Action {

    private AccountStoreManager manager;

    @Option(name = "-s",
            aliases = "--store",
            description = "Name of the store where account will be created")
    private String storeName;

    @Argument(name = "login",
              required = true,
              description = "Login of the new account")
    private String login;

    public CreateAccountAction(@Requires AccountStoreManager manager) {
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

        ServiceHandle<ModifiableAccountStore> handle = manager.findAccountStore(storeName, ModifiableAccountStore.class);
        if (handle == null) {
            throw new Exception(
                    String.format("Cannot find AccountStore named '%s'", storeName)
            );
        }
        try {
            ModifiableAccountStore store = handle.get();

            System.out.printf("  Enter password: ");
            System.out.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(session.getKeyboard()));
            String value = reader.readLine();
            store.createAccount(login, value);
            System.out.println();
            // TODO Ask for other attributes ?
            // * activated, full name, ...

            // TODO Print a recap

            return null;
        } finally {
            handle.release();
        }
    }
}
