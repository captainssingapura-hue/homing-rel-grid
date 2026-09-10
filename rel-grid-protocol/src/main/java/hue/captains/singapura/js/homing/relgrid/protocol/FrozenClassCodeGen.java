package hue.captains.singapura.js.homing.relgrid.protocol;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;

import java.lang.reflect.RecordComponent;
import java.util.Collection;

/**
 * A {@link DefinitionCodeGen} that emits a Java record's shape as a JS class
 * which is a <b>value object all the way down</b> — RFC 0050 · Episode 2,
 * ext4's rules and ext5's law 213.
 *
 * <p>Hand-written rather than reflective-from-core, and that is a first-class
 * choice under the CodeGen as Functions doctrine: provenance is invisible at
 * the contract boundary. It exists because core's {@code EcmaDefinitionCodeGen}
 * freezes the instance and not a collection component, and ext4's freeze has to
 * be deep or a value object is one dereference from mutable. Core is consumed
 * as a release here, so the fix lives where the requirement does.</p>
 *
 * <p>For {@code R(a, bs)} where {@code bs} is a collection it emits:</p>
 *
 * <pre>{@code
 * class R {
 *     constructor(a, bs) {
 *         this.a = a;
 *         this.bs = Object.freeze((bs || []).slice());
 *         Object.freeze(this);
 *     }
 * }
 * }</pre>
 *
 * <p>A collection is copied before it is frozen, so freezing what the caller
 * handed in is never a side effect on the caller's own array.</p>
 *
 * <p>Codecs are not this class's business. Nothing serialises a protocol value
 * yet, and ext5's law 214 says a target is not generated until something needs
 * it.</p>
 */
public record FrozenClassCodeGen() implements DefinitionCodeGen {

    public static final FrozenClassCodeGen INSTANCE = new FrozenClassCodeGen();

    @Override
    public String generate(ObjectDefinition<?> definition) {
        Class<?> type = definition.type();
        if (!type.isRecord())
            throw new IllegalArgumentException("FrozenClassCodeGen handles records only — got " + type);

        RecordComponent[] components = type.getRecordComponents();
        String name = type.getSimpleName();

        var sb = new StringBuilder();
        sb.append("/** Generated from ").append(type.getName()).append(" — do not edit. */\n");
        sb.append("class ").append(name).append(" {\n");
        sb.append("    constructor(");
        for (int i = 0; i < components.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(components[i].getName());
        }
        sb.append(") {\n");
        for (RecordComponent c : components) {
            String n = c.getName();
            sb.append("        this.").append(n).append(" = ");
            if (isCollection(c)) sb.append("Object.freeze((").append(n).append(" || []).slice())");
            else                 sb.append(n);
            sb.append(";\n");
        }
        sb.append("        Object.freeze(this);\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static boolean isCollection(RecordComponent c) {
        Class<?> t = c.getType();
        return t.isArray() || Collection.class.isAssignableFrom(t);
    }
}
