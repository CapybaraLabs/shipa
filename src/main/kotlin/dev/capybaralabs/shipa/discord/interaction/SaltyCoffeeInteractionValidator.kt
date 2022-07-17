package dev.capybaralabs.shipa.discord.interaction

import java.security.PublicKey
import software.pando.crypto.nacl.Crypto

/**
 * Based on https://github.com/NeilMadden/salty-coffee
 */
class SaltyCoffeeInteractionValidator(publicKeyString: String) : InteractionValidator {

	private val publicKey: PublicKey = Crypto.signingPublicKey(hexStringToByteArray(publicKeyString))

	override fun validateSignature(signature: String, timestamp: String, body: String): Boolean {
		return Crypto.signVerify(publicKey, (timestamp + body).toByteArray(), hexStringToByteArray(signature))
	}

	private fun hexStringToByteArray(s: String): ByteArray {
		val len = s.length
		val data = ByteArray(len / 2)
		var i = 0
		while (i < len) {
			data[i / 2] = ((digit(s[i]) shl 4) + (digit(s[i + 1]))).toByte()
			i += 2
		}
		return data
	}

	/**
	 * Compat function for Javas [Character.digit]
	 */
	private fun digit(c: Char): Int {
		return c.digitToIntOrNull(16) ?: -1
	}
}

