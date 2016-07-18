package org.rust.lang.core.stubs.elements


import com.intellij.psi.stubs.*
import org.rust.lang.core.psi.RustImplItemElement
import org.rust.lang.core.psi.RustPathTypeElement
import org.rust.lang.core.psi.impl.RustImplItemElementImpl
import org.rust.lang.core.resolve.indexes.RustImplIndex
import org.rust.lang.core.stubs.RustElementStub
import org.rust.lang.core.stubs.RustStubElementType
import org.rust.lang.core.symbols.RustQualifiedPath
import org.rust.lang.core.symbols.RustQualifiedPathPart
import org.rust.lang.core.symbols.impl.RustNamedQualifiedPathPart
import org.rust.lang.core.symbols.impl.RustSelfQualifiedPathPart
import org.rust.lang.core.symbols.impl.RustSuperQualifiedPathPart
import org.rust.lang.core.symbols.unfold
import sun.plugin.dom.exception.InvalidStateException


object RustImplItemStubElementType : RustStubElementType<RustImplItemElementStub, RustImplItemElement>("IMPL_ITEM") {

    override fun createStub(psi: RustImplItemElement, parentStub: StubElement<*>?): RustImplItemElementStub =
        RustImplItemElementStub(parentStub, this, (psi.type as? RustPathTypeElement)?.path)

    override fun createPsi(stub: RustImplItemElementStub): RustImplItemElement =
        RustImplItemElementImpl(stub, this)

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RustImplItemElementStub {
//        val ref = {
//            val parts = dataStream.readInt()
//            if (parts != -1) {
//                val fullyQualified = dataStream.readBoolean()
//                (0 until parts)
//                    .fold(null as RustQualifiedReference?, {
//                        qual, i ->
//                            dataStream.readName()?.let {
//                                val part = when (it.string) {
//                                    "super" -> RustSuperQualifiedReferencePart
//                                    "self"  -> RustSelfQualifiedReferencePart
//                                    else    -> RustNamedQualifiedReferencePart(it.string)
//                                }
//
//                                RustQualifiedReference.create(part, prefix = qual, fullyQualified = fullyQualified)
//                            }
//                                // TODO(kudinkin): Replace with a proper deserialization exception
//                                ?: throw InvalidStateException("Panic! Caches are corrupted!")
//                    })
//            } else {
//                null
//            }
//        }()

        return RustImplItemElementStub(parentStub, this, RustQualifiedPath.deserialize(dataStream))
    }

//    override fun serialize(stub: RustImplItemElementStub, dataStream: StubOutputStream) = with(dataStream) {
//        val ref = stub.ref
//        if (ref != null) {
//            val unfolded = ref.unfold().toList()
//
//            writeInt(unfolded.size)
//            writeBoolean(ref.fullyQualified)
//
//            unfolded.forEach {
//                writeName(it.name)
//            }
//        } else {
//            writeInt(-1)
//        }
//    }

    override fun serialize(stub: RustImplItemElementStub, dataStream: StubOutputStream) {
        RustQualifiedPath.serialize(stub.ref, dataStream)
    }

    override fun indexStub(stub: RustImplItemElementStub, sink: IndexSink) {
        stub.ref?.let {
            sink.occurrence(RustImplIndex.KEY, it.part.name)
        }
    }

}

class RustImplItemElementStub(
    parent: StubElement<*>?,
    elementType: IStubElementType<*, *>,
    val ref: RustQualifiedPath?
) : RustElementStub<RustImplItemElement>(parent, elementType)
