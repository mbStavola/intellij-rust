package org.rust.lang.core.types.unresolved

import org.rust.lang.core.types.visitors.RustUnresolvedTypeVisitor
import java.io.DataInput
import java.io.DataOutput

interface RustUnresolvedType {

    companion object {
        fun serialize(type: RustUnresolvedType?, output: DataOutput) {
            throw UnsupportedOperationException()
        }

        fun deserialize(input: DataInput): RustUnresolvedType? {
            throw UnsupportedOperationException()
        }
    }

    fun <T> accept(visitor: RustUnresolvedTypeVisitor<T>): T

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

}

