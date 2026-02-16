/*
 * Copyright (c) 2025 Nebulit GmbH
 * Licensed under the MIT License.
 */

const { generators } = require("../../app");

class ClassesGenerator {

    static generateDataClass(name, fields) {
        return `data class ${name}(${this.generateVariables(fields, ",\n")})`
    }

    static generateVariables(fields, separator = "\n") {

        return fields?.map((variable) => {
            if (variable.cardinality?.toLowerCase() === "list") {
                return `\tvar ${variable.name}:${typeMapping(variable.type, variable.cardinality, variable.optional, false, variable)}`;
            } else {
                if (variable.type?.toLowerCase() === "date") {
                    return `\t@JsonFormat(pattern = "dd.MM.yyyy") var ${variable.name}:${typeMapping(variable.type, variable.cardinality, variable.optional, false, variable)}`;
                } else if (variable.type?.toLowerCase() === "datetime") {
                    return `\t@JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss") var ${variable.name}:${typeMapping(variable.type, variable.cardinality, variable.optional, false, variable)}`;
                } else {
                    return `\tvar ${variable.name}:${typeMapping(variable.type, variable.cardinality, variable.optional, false, variable)}`;

                }
            }
        }).join(separator)
    }

}


function idType(element) {
    var idField = element.fields?.find(it => it.idAttribute)
    return idField ? typeMapping(idField.type, idField.cardinality, idField.optional, idField.mutable) : "java.util.UUID"
}

const customItemTypeName = (fieldName) => {
    return `${fieldName}Item`
}

const typeMapping = (fieldType, fieldCardinality, optional, mutable, field) => {
    var resolvedType;
    // Handle Custom type with subfields → generate item class name
    if (fieldType?.toLowerCase() === "custom" && field?.subfields?.length > 0) {
        resolvedType = customItemTypeName(field.name)
    } else {
        switch (fieldType?.toLowerCase()) {
            case "string":
                resolvedType = optional ? "String?" : "String";
                break
            case "double":
                resolvedType = optional ? "Double?" : "Double";
                break
            case "int":
                resolvedType = optional ? "Int?" : "Int";
                break
            case "long":
                resolvedType = optional ? "Long?" : "Long";
                break
            case "boolean":
                resolvedType = optional ? "Boolean?" : "Boolean";
                break
            case "date":
                resolvedType = optional ? "LocalDate?" : "LocalDate";
                break
            case "datetime":
                resolvedType = optional ? "LocalDateTime?" : "LocalDateTime";
                break
            case "uuid":
                resolvedType = optional ? "UUID?" : "UUID";
                break
            default:
                resolvedType = optional ? "String?" : "String";
                break
        }
    }
    if (fieldCardinality?.toLowerCase() === "list") {
        return mutable ? `MutableList<${resolvedType}>` : `List<${resolvedType}>`
    } else {
        return resolvedType
    }

}

const typeImports = (fields, additionalImports, rootPackageName) => {
    if (!fields || fields.length === 0) {
        return []
    }
    var imports = fields?.map((field) => {
        var result = []
        switch (field.type?.toLowerCase()) {
            case "date":
                result = ["import java.time.LocalDate", "import org.springframework.format.annotation.DateTimeFormat", "import com.fasterxml.jackson.annotation.JsonFormat"]
                break
            case "datetime":
                result = ["import java.time.LocalDateTime", "import org.springframework.format.annotation.DateTimeFormat", "import com.fasterxml.jackson.annotation.JsonFormat"]
                break
            case "uuid":
                result = ["import java.util.UUID"]
                break
            case "custom":
                if (field.subfields?.length > 0 && rootPackageName) {
                    result = [`import ${rootPackageName}.common.${customItemTypeName(field.name)}`]
                }
                break
        }
        switch (field.cardinality?.toLowerCase()) {
            case "list":
                result.push("import kotlin.collections.List")
                break
        }
        return result
    }).concat(additionalImports)
    return Array.from([...new Set(imports?.flat() ?? [])]).flat().join(";\n")

}

module.exports = { ClassesGenerator, typeMapping, typeImports, idType, customItemTypeName }
