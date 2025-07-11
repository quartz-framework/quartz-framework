package xyz.quartzframework.core.bean.definition.metadata;

import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeParameter;
import lombok.Builder;
import lombok.Value;
import lombok.val;

import java.util.Collections;
import java.util.List;

import static xyz.quartzframework.core.util.ClassUtil.extractPackageName;
import static xyz.quartzframework.core.util.ClassUtil.extractSimpleName;

@Value
@Builder
public class ParameterMetadata {

    String fullName;
    String simpleName;
    String packageName;

    @Builder.Default
    List<ParameterMetadata> parameters = Collections.emptyList();

    public static ParameterMetadata of(Class<?> clazz) {
        return ParameterMetadata.builder()
                .fullName(clazz.getName())
                .simpleName(extractSimpleName(clazz.getName()))
                .packageName(extractPackageName(clazz.getName()))
                .build();
    }

    public static ParameterMetadata of(TypeParameter typeParameter) {
        val bound = typeParameter.getClassBound();
        if (bound == null) {
            return ParameterMetadata.builder()
                    .fullName(typeParameter.getName())
                    .simpleName(typeParameter.getName())
                    .packageName("")
                    .build();
        }

        val nested = of(bound.toString());
        return ParameterMetadata.builder()
                .fullName(typeParameter.getName())
                .simpleName(typeParameter.getName())
                .packageName("")
                .parameters(List.of(nested))
                .build();
    }

    public static ParameterMetadata of(TypeArgument typeArgument) {
        val sig = typeArgument.getTypeSignature();
        if (sig == null) {
            return ParameterMetadata.builder()
                    .fullName("?")
                    .simpleName("?")
                    .packageName("")
                    .build();
        }

        val name = sig.toString();

        return ParameterMetadata.builder()
                .fullName(name)
                .simpleName(extractSimpleName(name))
                .packageName(extractPackageName(name))
                .parameters(List.of())
                .build();
    }

    public static ParameterMetadata of(String name) {
        return ParameterMetadata.builder()
                .fullName(name)
                .simpleName(extractSimpleName(name))
                .packageName(extractPackageName(name))
                .build();
    }
} 
