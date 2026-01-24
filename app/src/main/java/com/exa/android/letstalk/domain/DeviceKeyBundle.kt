package com.exa.android.letstalk.domain

/**
 * Data model for device public key bundle stored in Firestore.
 * Contains all public keys needed for X3DH key agreement.
 *
 * This bundle is uploaded to Firestore at: users/{userId}/devices/{deviceId}
 *
 * @property deviceId Unique device identifier
 * @property registrationId Random ID from Signal Protocol
 * @property identityKey Base64-encoded public identity key
 * @property signedPreKeyId ID of the signed prekey
 * @property signedPreKeyPublic Base64-encoded signed prekey public part
 * @property signedPreKeySignature Base64-encoded signature of signed prekey
 * @property preKeyId Optional one-time prekey ID (null if consumed)
 * @property preKeyPublic Optional Base64-encoded one-time prekey public part
 */
data class DeviceKeyBundle(
    val deviceId: Int = 0,
    val registrationId: Int = 0,
    val identityKey: String = "",
    val signedPreKeyId: Int = 0,
    val signedPreKeyPublic: String = "",
    val signedPreKeySignature: String = "",
    val preKeyId: Int? = null,
    val preKeyPublic: String? = null
) {
    companion object {
        /**
         * Checks if the bundle has enough keys for session establishment
         */
        fun DeviceKeyBundle.isValid(): Boolean {
            return deviceId != 0 &&
                    registrationId != 0 &&
                    identityKey.isNotEmpty() &&
                    signedPreKeyId != 0 &&
                    signedPreKeyPublic.isNotEmpty() &&
                    signedPreKeySignature.isNotEmpty()
        }
    }
}