plugins {
	java
	id("org.springframework.boot") version "3.3.10"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.app"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/snapshot") }
	maven {
		url = uri("https://ec.europa.eu/cefdigital/artifact/content/repositories/esignaturedss/")
	}
	flatDir {
		dirs("libs")
	}

}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("javax.xml.bind:jaxb-api:2.3.1")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
	implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
	// https://mvnrepository.com/artifact/eu.europa.ec.joinup.sd-dss/dss-service
	implementation("eu.europa.ec.joinup.sd-dss:dss-service:6.1")
	implementation("org.bouncycastle:bcprov-jdk18on:1.77")
	// https://mvnrepository.com/artifact/eu.europa.ec.joinup.sd-dss/dss-xades
	implementation("eu.europa.ec.joinup.sd-dss:dss-xades:6.1")
	// https://mvnrepository.com/artifact/eu.europa.ec.joinup.sd-dss/dss-token
	implementation("eu.europa.ec.joinup.sd-dss:dss-token:6.1")
	// https://mvnrepository.com/artifact/eu.europa.ec.joinup.sd-dss/dss-model
	implementation("eu.europa.ec.joinup.sd-dss:dss-model:6.1")
	// https://mvnrepository.com/artifact/eu.europa.ec.joinup.sd-dss/dss-crl-parser
	implementation("eu.europa.ec.joinup.sd-dss:dss-crl-parser:6.1")
	// https://mvnrepository.com/artifact/eu.europa.ec.joinup.sd-dss/dss-utils-apache-commons
	implementation("eu.europa.ec.joinup.sd-dss:dss-utils-apache-commons:6.1")
	// https://mvnrepository.com/artifact/commons-io/commons-io
	implementation("commons-io:commons-io:2.18.0")
	// https://mvnrepository.com/artifact/eu.europa.ec.joinup.sd-dss/dss-asic-common
	implementation("eu.europa.ec.joinup.sd-dss:dss-asic-common:6.1")
	// https://mvnrepository.com/artifact/eu.europa.ec.joinup.sd-dss/dss-policy-jaxb
	implementation("eu.europa.ec.joinup.sd-dss:dss-policy-jaxb:6.1")
	// https://mvnrepository.com/artifact/eu.europa.ec.joinup.sd-dss/dss-validation
	implementation("eu.europa.ec.joinup.sd-dss:dss-validation:6.1")
	implementation(files("libs/bcmail-jdk16-1.45.jar"))
	implementation(files("libs/bcprov-jdk16-1.45.jar"))
	implementation(files("libs/bctsp-jdk16-1.45.jar"))
	implementation(files("libs/commons-codec-1.2.jar"))
	implementation(files("libs/commons-httpclient-3.0.1.jar"))
	implementation(files("libs/commons-lang-2.4.jar"))
	implementation(files("libs/commons-logging-1.1.1.jar"))
	implementation(files("libs/MITyCLibAPI-1.1.7.jar"))
	implementation(files("libs/MITyCLibCert-1.1.7.jar"))
	implementation(files("libs/MITyCLibCrypt-1.1.7.jar"))
	implementation(files("libs/MITyCLibOCSP-1.1.7.jar"))
	implementation(files("libs/MITyCLibPolicy-1.1.7.jar"))
	implementation(files("libs/MITyCLibTrust-1.1.7.jar"))
	implementation(files("libs/MITyCLibTSA-1.1.7.jar"))
	implementation(files("libs/MITyCLibXADES-1.1.7.jar"))
	implementation(files("libs/xml-apis-1.3.04.jar"))
	implementation(files("libs/xmlsec-1.4.2-ADSI-1.0.jar"))
	implementation(files("libs/xmlsec-1.4.2-ADSI-1.1.jar"))
	// https://mvnrepository.com/artifact/org.mapstruct/mapstruct-processor
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	implementation("org.mapstruct:mapstruct:1.6.3")
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt
	implementation("io.jsonwebtoken:jjwt:0.12.6")
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	// Apache POI for Excel reading
	implementation("org.apache.poi:poi:5.2.5")
	implementation("org.apache.poi:poi-ooxml:5.2.5")
    // PDF desde HTML con Flying Saucer (usa OpenPDF por compatibilidad con iText 2.x)
    implementation("org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.4.0")
    // Envío de correos
    implementation("org.springframework.boot:spring-boot-starter-mail")
    // Motor de plantillas HTML (para el diseño del PDF)
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    // Soporte para ejecución asíncrona
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")
    // https://mvnrepository.com/artifact/com.openhtmltopdf/openhtmltopdf-pdfbox
    implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")
    // https://mvnrepository.com/artifact/com.openhtmltopdf/openhtmltopdf-slf4j
    implementation("com.openhtmltopdf:openhtmltopdf-slf4j:1.0.10")
	// Source: https://mvnrepository.com/artifact/net.sf.jasperreports/jasperreports
	implementation("net.sf.jasperreports:jasperreports:6.20.6")
	// Source: https://mvnrepository.com/artifact/net.sf.barcode4j/barcode4j
	implementation("net.sf.barcode4j:barcode4j:2.1")
	// Source: https://mvnrepository.com/artifact/net.sf.jasperreports/jasperreports-fonts
	implementation("net.sf.jasperreports:jasperreports-fonts:6.20.6")
	// Source: https://mvnrepository.com/artifact/net.sf.jasperreports/jasperreports-functions
	implementation("net.sf.jasperreports:jasperreports-functions:6.20.6")
	// Source: https://mvnrepository.com/artifact/org.apache.xmlgraphics/batik-bridge
	implementation("org.apache.xmlgraphics:batik-bridge:1.18")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
