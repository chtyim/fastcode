package org.fastcode.type;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class for representing type information of the given type {@code T}
 */
public abstract class GenuineType<T> {

    private final Type type;
    private final Class<?> rawClass;

    public static GenuineType<?> of(Type type) {
        return new GenuineType<Object>(type) { };
    }

    protected GenuineType() {
        Map<String, Type> resolvedTypes = new HashMap<String, Type>();
        Class<?> clz = getClass();
        do {
            Type type = clz.getGenericSuperclass();
            clz = clz.getSuperclass();
            propulateResolvedTypes(type, clz, resolvedTypes);
        } while (!clz.equals(GenuineType.class));

        type = resolvedTypes.get(clz.getTypeParameters()[0].getName());
        rawClass = resolveRawClass(type);
    }

    private GenuineType(Type type) {
        this.type = type;
        this.rawClass = resolveRawClass(type);
    }

    public final Type getType() {
        return type;
    }

    public final Class<?> getRawClass() {
        return rawClass;
    }

    public GenuineType<?> getReturnType(Method method) {
        return resolveType(method.getGenericReturnType(), getDeclaringClass(method));
    }

    public List<GenuineType<?>> getParameterTypes(Method method) {
        return resolveTypes(getDeclaringClass(method), method.getGenericParameterTypes());
    }

    public List<GenuineType<?>> getExceptionTypes(Method method) {
        return resolveTypes(getDeclaringClass(method), method.getGenericExceptionTypes());
    }

    private List<GenuineType<?>> resolveTypes(Class<?> declaringClass, Type[] params) {
        List<GenuineType<?>> result = new ArrayList<GenuineType<?>>(params.length);
        for (Type param : params) {
            result.add(resolveType(param, declaringClass));
        }
        return result;
    }

    private Class<?> getDeclaringClass(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (!declaringClass.isAssignableFrom(rawClass)) {
            throw new IllegalArgumentException(String.format("Method %s is not declared in %s or its superclass", method, rawClass));
        }
        return declaringClass;
    }

    private GenuineType<?> resolveType(Type targetType, Class<?> declaringClass) {
        // DFS for declaring class
        Map<String, Type> resolvedTypes = new HashMap<String, Type>();
        Deque<Type> stack = new LinkedList<Type>();
        Class<?> clz;
        stack.push(type);
        do {
            Type type = stack.pop();
            clz = resolveRawClass(type);

            propulateResolvedTypes(type, clz, resolvedTypes);

            Type superType = clz.getGenericSuperclass();
            if (superType != null) {
                stack.push(superType);
            }
            for (Type t : clz.getGenericInterfaces()) {
                stack.push(t);
            }
        } while (!clz.equals(declaringClass));

        return of(resolveType(targetType, resolvedTypes));
    }

    @Override
    public String toString() {
        return String.format("GenuineType{type=%s, rawClass=%s}", type, rawClass);
    }

    private void propulateResolvedTypes(Type type, Class<?> clz, Map<String, Type> resolvedTypes) {
        if (type instanceof ParameterizedType) {
            TypeVariable<? extends Class<?>>[] typeParams = clz.getTypeParameters();
            Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
            for (int i = 0; i < typeParams.length; i++) {
                resolvedTypes.put(typeParams[i].getName(), resolveType(typeArgs[i], resolvedTypes));
            }
        }
    }

    private Class<?> resolveRawClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>)type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType)type).getRawType();
        }
        throw new IllegalArgumentException("Failed to resolve raw class.");
    }

    private Type[] resolveTypeArguments(Type[] types, Map<String, Type> resolvedTypes) {
        for (int i = 0; i < types.length; i++) {
            types[i] = resolveType(types[i], resolvedTypes);
        }
        return types;
    }

    private Type resolveType(Type type, Map<String, Type> resolvedTypes) {
        if (type instanceof Class) {
            return type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType)type;
            return new ParameterizedTypeImpl(resolveTypeArguments(ptype.getActualTypeArguments(), resolvedTypes), ptype.getRawType(), ptype.getOwnerType());
        } else if (type instanceof TypeVariable) {
            String name = ((TypeVariable<?>)type).getName();
            Type resultType = resolvedTypes.get(name);
            if (resultType == null) {
                throw new IllegalArgumentException(String.format("Failed to resolve type %s", type));
            }
            return resultType;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported type resolve: %s", type));
        }
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {

        private final Type[] typeArgs;
        private final Type rawType;
        private final Type ownerType;
        private final String string;

        ParameterizedTypeImpl(Type[] typeArgs, Type rawType, Type ownerType) {
            this.typeArgs = typeArgs;
            this.rawType = rawType;
            this.ownerType = ownerType;
            this.string = toString(this);
        }

        @Override
        public Type[] getActualTypeArguments() {
            return Arrays.copyOf(typeArgs, typeArgs.length);
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public String toString() {
            return String.format("ParameterizedType %s", string);
        }

        private String toString(Type type) {
            if (type instanceof Class<?>) {
                return ((Class<?>)type).getName();
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                StringBuilder builder = new StringBuilder(toString(parameterizedType.getRawType())).append('<');
                Type[] typeArgs = parameterizedType.getActualTypeArguments();
                builder.append(toString(typeArgs[0]));
                for (int i = 1; i < typeArgs.length; i++) {
                    builder.append(',').append(toString(typeArgs[i]));
                }
                return builder.append('>').toString();
            } else {
                return type.toString();
            }
        }
    }
}
