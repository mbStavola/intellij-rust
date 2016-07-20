package org.rust.lang.core.types

import com.intellij.openapi.project.Project
import org.rust.lang.core.psi.RustImplItemElement
import org.rust.lang.core.psi.RustImplMethodMemberElement
import org.rust.lang.core.resolve.indexes.RustImplIndex
import org.rust.lang.core.types.util.decay
import org.rust.lang.core.types.visitors.RustTypeVisitor

interface RustType {

    fun <T> accept(visitor: RustTypeVisitor<T>): T

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String

//    val impls: Sequence<RustImplItemElement>
//        get() = RustImplIndex.findImplsFor(this, ?)
//
//    val nonStaticMethods: Sequence<RustImplMethodMemberElement>
//        get() = methods.filter { !it.isStatic }
//
//    val staticMethods: Sequence<RustImplMethodMemberElement>
//        get() = methods.filter { it.isStatic }
//
//    private val methods: Sequence<RustImplMethodMemberElement>
//        get() = impls.flatMap { it.implBody?.implMethodMemberList.orEmpty().asSequence() }
}
