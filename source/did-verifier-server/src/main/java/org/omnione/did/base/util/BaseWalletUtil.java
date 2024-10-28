/*
 * Copyright 2024 OmniOne.
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

package org.omnione.did.base.util;

import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.wallet.exception.WalletException;
import org.omnione.did.wallet.key.WalletManagerFactory;
import org.omnione.did.wallet.key.WalletManagerFactory.WalletManagerType;
import org.omnione.did.wallet.key.WalletManagerInterface;

import java.nio.charset.StandardCharsets;

/**
 * Utility class for wallet operations.
 * This class provides methods for creating, connecting to, and managing wallets,
 * as well as performing cryptographic operations using wallet keys.
 */
@Slf4j
public class BaseWalletUtil {

    /**
     * Connect to an existing file wallet with the given file path and password.
     * The wallet manager is used to perform cryptographic operations using the wallet keys.
     *
     * @param walletFilePath Path to the wallet file
     * @param password Password to unlock the wallet
     * @return Wallet manager interface for the connected wallet
     *
     */
    public static WalletManagerInterface connectFileWallet(String walletFilePath, String password) throws WalletException {

        WalletManagerInterface walletManager = WalletManagerFactory.getWalletManager(WalletManagerType.FILE);
        walletManager.connect(walletFilePath, password.toCharArray());

        return walletManager;

    }

    /**
     * Retrieves an instance of a file-based wallet manager.
     * The wallet manager is responsible for handling cryptographic operations
     * and key management using the keys stored in a file-based wallet.
     *
     * This method attempts to get a wallet manager of type FILE using the
     * WalletManagerFactory. If the wallet manager cannot be retrieved, an
     * OpenDidException is thrown.
     *
     * @return WalletManagerInterface Instance of the file-based wallet manager
     * @throws OpenDidException if the wallet manager cannot be retrieved
     */
    public static WalletManagerInterface getFileWalletManager() {
        try {
            return WalletManagerFactory.getWalletManager(WalletManagerType.FILE);
        } catch (WalletException e) {
            log.error("Failed to get Wallet Manager: {}", e.getMessage());
            throw new OpenDidException(ErrorCode.FAILED_TO_GET_FILE_WALLET_MANAGER);
        }
    }


    /**
     * Generate a compact signature for the given plain text using the key with the given key id.
     * The plain text is first converted to a byte array using UTF-8 encoding.
     *
     * @param walletManager Wallet manager interface for the connected wallet
     * @param keyId Key ID of the key to use for signing
     * @param plainText Plain text to sign
     * @return Compact signature as a byte array
     * @throws WalletException if the signature generation fails
     */
    public static byte[] generateCompactSignature(WalletManagerInterface walletManager, String keyId, String plainText) throws WalletException {
        return generateCompactSignature(walletManager, keyId, plainText.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a compact signature for the given plain text using the key with the given key id.
     * The plain text is first converted to a byte array using UTF-8 encoding.
     *
     * @param walletManager Wallet manager interface for the connected wallet
     * @param keyId Key ID of the key to use for signing
     * @param plainText Plain text to sign
     * @return Compact signature as a byte array
     * @throws OpenDidException if the signature generation fails
     */
    public static byte[] generateCompactSignature(WalletManagerInterface walletManager, String keyId, byte[] plainText) throws WalletException {

            return walletManager.generateCompactSignatureFromHash(keyId, BaseDigestUtil.generateHash(plainText));

    }


}
