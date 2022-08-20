package dev.capybaralabs.shipa.discord.model

class IntBitfield<E : IntBitflag>(elements: Collection<E>) : HashSet<E>(elements) {

	companion object {
		fun <F : IntBitflag> of(vararg elements: F): IntBitfield<F> {
			return IntBitfield(elements.asList())
		}
	}

	infix fun or(other: IntBitfield<E>?): IntBitfield<E> {
		return IntBitfield(this union (other ?: setOf()))
	}

}

interface IntBitflag {
	val value: Int
}


// for bitfields that are represented as strings by discord, e.g. permissions. needs to use BigInteger or some other larger-than-Int representation
class StringBitfield<E : StringBitflag>(elements: Collection<E>) : HashSet<E>(elements) {

	companion object {
		fun <F : StringBitflag> of(vararg elements: F): StringBitfield<F> {
			return StringBitfield(elements.asList())
		}
	}

	infix fun or(other: StringBitfield<E>?): StringBitfield<E> {
		return StringBitfield(this union (other ?: setOf()))
	}

}

interface StringBitflag {
	val value: Long
}
