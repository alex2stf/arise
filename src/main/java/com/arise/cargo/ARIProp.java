package com.arise.cargo;

public class ARIProp extends ARILined implements Comparable<ARIProp> {



    public static ARIProp column(ARIClazz parent, String name, PrimitiveType primitiveType) {
        ARIProp columnProp = new ARIProp.Builder(parent, name)
            .setTypeName(primitiveType.name().toLowerCase())
            .setAlias(name)
            .build();
        return columnProp;
    }



    @Override
    public String toString() {
        return "P{" +
             typeName + '|' +
             name + '|' +
             isArray + "|" +
             alias + '|' +
             noGet + "|" +
             noSet +"|" +
             accessType +"|" +
             _isFinal +"|" +
             isStatic +"|" +
             isTranzient +"|" +
             pKey +"|" +
             unique +"|" +
             nullable +"|" +
             fetchType + '|' +
             maxLength +"|" +
             minLength +"|" +
             defaultValue + '|' +
             parentClass +
            '}';
    }


    private final String typeName;
    private final String name;
    private final boolean isArray;
    private final String alias;
    private final boolean noGet;
    private final boolean noSet;
    private final AccessType accessType;
    private final boolean _isFinal;
    private final boolean isStatic;
    private final boolean isTranzient;
    private final boolean _volatile;
    private final boolean pKey;
    private final boolean unique;
    private final boolean nullable;
    private final String fetchType;
    private final Integer maxLength;
    private final Integer minLength;
    private final String defaultValue;

    private final ARIClazz parentClass;

    public ARIProp(ARIClazz parent, String typeName, String name, boolean isArray,
        String alias, boolean noGet, boolean noSet, AccessType accessType, boolean _isFinal,
        boolean isStatic, boolean isTranzient, boolean pKey,
        boolean unique, String fetchType, Integer maxLength, Integer minLength, String defaultValue, boolean nullable, boolean isVolatile) {
        this.typeName = typeName;
        this.name = name;
        this.isArray = isArray;
        this.alias = alias;
        this.noGet = noGet;
        this.noSet = noSet;
        this.accessType = accessType;
        this._isFinal = _isFinal;
        this.isStatic = isStatic;
        this.isTranzient = isTranzient;
        this.pKey = pKey;
        this.unique = unique;
        this.fetchType = fetchType;
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.defaultValue = defaultValue;
        this.parentClass = parent;
        this.nullable = nullable;
        this._volatile = isVolatile;
    }


    public static ARIProp cloneForDTO(String newName, ARIProp s){
        return new ARIProp(s.parentClass, s.typeName, newName, s.isArray, s.alias, false, false, s.accessType, false, false,
            s.isTranzient, s.pKey,
            s.unique, s.fetchType, s.maxLength, s.minLength, s.defaultValue, false, false);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getName() {
        return name;
    }


    public boolean isArray() {
        return isArray;
    }

    public String getAlias() {
        return alias;
    }

    public boolean isNoGet() {
        return noGet;
    }

    public boolean isNoSet() {
        return noSet;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public boolean isFinal() {
        return _isFinal;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isTranzient() {
        return isTranzient;
    }

    public boolean isPrimaryKey() {
        return pKey;
    }

    public boolean isUnique() {
        return unique;
    }

    public String getFetchType() {
        return fetchType;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public boolean isVolatile(){
        return _volatile;
    }

    public ARIType getType(Context self) {
        return self.getTypeByName(typeName);
    }

    @Override
    public int compareTo(ARIProp o) {
        if(getName()!= null && o != null) {
            return getName().compareTo(o.getName());
        }
        return 0;
    }

    public ARIClazz getParent() {
        return parentClass;
    }


    public boolean allowGet() {
        if(isNoGet() || AccessType.PUBLIC.equals(getAccessType())){
            return false;
        }
        return true;
    }

    public boolean allowSet() {
        if (_isFinal || isNoSet() || AccessType.PUBLIC.equals(getAccessType())){
            return false;
        }
        return true;
    }

    public ARIClazz getTable() {
        return parentClass;
    }


    public boolean isNullable() {
        return nullable;
    }

    public boolean isPrimitive(Context context) {
        return context.isPrimitive(this);
    }




    public static class Builder {

        private final ARIClazz parent;
        private String typeName;
        private final String name;
        private boolean isArray;
        private String alias;
        private boolean noGet;
        private boolean noSet;
        private AccessType accessType;
        private boolean isFinal;
        private boolean isStatic;
        private boolean isTranzient;
        private boolean pKey;
        private boolean unique;
        private String fetchType;
        private Integer maxLength;
        private Integer minLength;
        private String defaultValue;
        private boolean nullable;

        private boolean isVolatile;

        public Builder(ARIClazz parent, String name) {
            this.parent = parent;
            this.name = name;
        }


        public Builder setTypeName(String typeName) {
            this.typeName = typeName;
            return this;
        }


        public Builder setVolatile(boolean isVolatile) {
            this.isVolatile = isVolatile;
            return this;
        }


        public Builder setIsArray(boolean isArray) {
            this.isArray = isArray;
            return this;
        }

        public Builder setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder setNoGet(boolean noGet) {
            this.noGet = noGet;
            return this;
        }

        public Builder setNoSet(boolean noSet) {
            this.noSet = noSet;
            return this;
        }

        public Builder setAccessType(AccessType accessType) {
            this.accessType = accessType;
            return this;
        }

        public Builder setIsFinal(boolean isFinal) {
            this.isFinal = isFinal;
            return this;
        }

        public Builder setIsStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }

        public Builder setIsTranzient(boolean isTranzient) {
            this.isTranzient = isTranzient;
            return this;
        }

        public Builder setIsPrimaryKey(boolean pKey) {
            this.pKey = pKey;
            return this;
        }

        public Builder setUnique(boolean unique) {
            this.unique = unique;
            return this;
        }

        public Builder setFetchType(String fetchType) {
            this.fetchType = fetchType;
            return this;
        }

        public Builder setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder setMinLength(Integer minLength) {
            this.minLength = minLength;
            return this;
        }

        public Builder setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder setNullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public ARIProp build() {
            return new ARIProp(parent, typeName, name, isArray, alias, noGet, noSet, accessType, isFinal, isStatic, isTranzient, pKey,
                unique, fetchType, maxLength, minLength, defaultValue, nullable, isVolatile);
        }

    }
}
