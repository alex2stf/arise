-- table clazz FileEntity
BEGIN
EXECUTE IMMEDIATE 'CREATE TABLE ent_file(
    deleted NUMBER,
    id NUMBER,
    mimeType VARCHAR2,
    name VARCHAR2,
    notes VARCHAR2,
    parentId NUMBER,
    path VARCHAR2,
    serviceId VARCHAR2,
    size NUMBER,
    status VARCHAR2
 )';
EXCEPTION
    WHEN OTHERS THEN
    IF SQLCODE = -955 THEN NULL; -- suppresses ORA-00955 exception
    ELSE RAISE;
    END IF;
    END; 
/

-- table clazz FolderEntity
BEGIN
EXECUTE IMMEDIATE 'CREATE TABLE ent_folder(
    creationDate DATE,
    deleted NUMBER,
    description VARCHAR2,
    id NUMBER,
    phase VARCHAR2,
    productCode VARCHAR2,
    productId NUMBER,
    productName VARCHAR2,
    source VARCHAR2,
    status VARCHAR2,
    type VARCHAR2
 )';
EXCEPTION
    WHEN OTHERS THEN
    IF SQLCODE = -955 THEN NULL; -- suppresses ORA-00955 exception
    ELSE RAISE;
    END IF;
    END; 
/

-- table clazz Tag
BEGIN
EXECUTE IMMEDIATE 'CREATE TABLE store_tags(
    id NUMBER,
    value VARCHAR2
 )';
EXCEPTION
    WHEN OTHERS THEN
    IF SQLCODE = -955 THEN NULL; -- suppresses ORA-00955 exception
    ELSE RAISE;
    END IF;
    END; 
/

-- table clazz Property
BEGIN
EXECUTE IMMEDIATE 'CREATE TABLE store_props(
    id NUMBER,
    key VARCHAR2,
    values VARCHAR2
 )';
EXCEPTION
    WHEN OTHERS THEN
    IF SQLCODE = -955 THEN NULL; -- suppresses ORA-00955 exception
    ELSE RAISE;
    END IF;
    END; 
/

-- table clazz FolderTag
BEGIN
EXECUTE IMMEDIATE 'CREATE TABLE d_tags(
    createdOn DATE
 )';
EXCEPTION
    WHEN OTHERS THEN
    IF SQLCODE = -955 THEN NULL; -- suppresses ORA-00955 exception
    ELSE RAISE;
    END IF;
    END; 
/

-- table clazz FileTag
BEGIN
EXECUTE IMMEDIATE 'CREATE TABLE f_tags(
    createdOn DATE
 )';
EXCEPTION
    WHEN OTHERS THEN
    IF SQLCODE = -955 THEN NULL; -- suppresses ORA-00955 exception
    ELSE RAISE;
    END IF;
    END; 
/

