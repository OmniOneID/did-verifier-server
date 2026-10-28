/*
 * Copyright 2026 OmniOne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnione.did.verifier.v1.protocol.registry;

import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.constant.ProtocolType;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.protocol.handler.ProtocolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProtocolRegistry {

    private final Map<ProtocolType, ProtocolHandler> handlers;

    public ProtocolRegistry(List<ProtocolHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(ProtocolHandler::getProtocolType, Function.identity()));
        log.info("Registered protocol handlers: {}", handlers.keySet());
    }

    public ProtocolHandler getHandler(ProtocolType protocolType) {
        ProtocolHandler handler = handlers.get(protocolType);
        if (handler == null) {
            throw new OpenDidException(ErrorCode.PROTOCOL_HANDLER_NOT_FOUND);
        }
        return handler;
    }
}
