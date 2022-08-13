package dev.capybaralabs.shipa.discord.model

class Bitfield<E : Bitflag>(elements: Collection<E>) : HashSet<E>(elements) {

	companion object {
		fun <F : Bitflag> of(vararg elements: F): Bitfield<F> {
			return Bitfield(elements.asList())
		}
	}

	infix fun or(other: Bitfield<E>?): Bitfield<E> {
		return Bitfield(this union (other ?: setOf()))
	}

}

interface Bitflag {
	val value: Int
}
