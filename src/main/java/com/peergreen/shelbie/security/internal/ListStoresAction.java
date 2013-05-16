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

import java.util.List;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.service.command.CommandSession;

import com.peergreen.security.realm.ModifiableAccountStore;
import com.peergreen.security.realm.manager.AccountStoreManager;
import com.peergreen.security.realm.manager.ServiceHandle;

/**
 * User: guillaume
 * Date: 15/03/13
 * Time: 22:03
 */
@Component
@Command(name = "list-stores",
        scope = "security",
        description = "List AccountStore(s) available in the system.")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class ListStoresAction implements Action {

    private AccountStoreManager manager;

    public ListStoresAction(@Requires AccountStoreManager manager) {
        this.manager = manager;
    }

    @Override
    public Object execute(CommandSession session) throws Exception {

        System.out.printf("AccountStore(s) of the system:%n");
        for (String name : manager.listStoreNames()) {
            System.out.printf(" * %s%n", name);
        }

        return null;
    }
}
