/**
 * <p>本文档全面描述了元数据系统的对外提供的REST API，包括使用方法和功能调用，文档预期的读者为数梦工场内部研发人员、平台应用开发人员、相关技术人员等。</p>
 */
@XmlSchema (
        namespace = "http://localhost:21000/api/atlas/",

        xmlns = {
                @javax.xml.bind.annotation.XmlNs(prefix = "admin", namespaceURI = "http://localhost:21000/api/atlas/admin"),
                @javax.xml.bind.annotation.XmlNs(prefix = "entities", namespaceURI = "http://localhost:21000/api/atlas/entities")
        }
)
@JsonSchema(schemaId = "webapp")
package org.apache.atlas.web.resources;

import org.codehaus.enunciate.json.JsonSchema;
import javax.xml.bind.annotation.XmlSchema;