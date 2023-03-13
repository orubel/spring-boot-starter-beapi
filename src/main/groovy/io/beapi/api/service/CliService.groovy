/*
 * Copyright 2013-2022 Owen Rubel
 * API Chaining(R) 2022 Owen Rubel
 *
 * Licensed under the AGPL v2 License;
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Owen Rubel (orubel@gmail.com)
 *
 *
 * ##### USAGE ####
 * for scaffolding a connector : gradle  scaffold -Pargs="domain=demo.application.domain.Company"
 * for scaffolding a controller: gradle  scaffold -Pargs="domain=demo.application.domain.Company controller=demo.application.controller"
 *
 */
package io.beapi.api.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.io.File

import groovy.text.Template
import groovy.text.GStringTemplateEngine

import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.annotation.Value;
import javax.persistence.EntityManager
import javax.persistence.metamodel.EntityType
import javax.persistence.metamodel.Attribute
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.stereotype.Controller
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.annotation.Annotation;

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class CliService {

	@Value("\${sun.java.command}")
	private List<String> argsString;

	@Value("\${api.iostateDir}")
	private connectorDir

	@Autowired
	private ListableBeanFactory listableBeanFactory;

	private ApplicationContext ctx

	private String controllerArg
	private String connectorArg
	private String domainArg

	private boolean connectorFound = false
	private boolean controllerFound = false
	private boolean domainFound = false;

	private LinkedHashMap createData = [:]
	private LinkedHashMap updateData = [:]

	/*
	 * BOOTSTRAP DATA
	 */
	String realName
	String realPackageName
	private LinkedHashMap data = [:]
	private Object obj;
	String dirPath
	String templateDir = "${System.getProperty('user.dir')}/src/main/groovy/templates/"
	List variables = []

	public CliService(ApplicationContext applicationContext) {
		this.ctx = applicationContext
	}

	static transactional = false

	void parse() {
		ArrayList args = argsString[0].split(" ")
		if(args.size()>0) {
			args.remove(0)
			ArrayList validArgKeys = ['controller', 'domain']
			LinkedHashMap temp = [:]
			args.each() {
				ArrayList z = it.split('=')
				temp.put(z[0],z[1])
			}

			ArrayList keys = temp.keySet()
			keys.removeAll(validArgKeys);
			if(args.size()>0 && keys.size()==0 && keys.isEmpty()) {
				temp.each() { k, v ->
					if (validArgKeys.contains(k.toLowerCase())) {

						if (v ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {

							switch (k.toLowerCase()) {
								case 'controller':
									if (controllerArg != null) {
										error(1, "'controller' value has already been set. Please try again.")
									} else {
										controllerArg = v
									}
									break
								case 'domain':
									if (domainArg != null) {
										error(1, "'domain' value has already been set. Please try again.")
									} else {
										domainArg = v
									}
									break
								default:
									error(1, "Unrecognized arg. Please try again.")
							}
						} else {
							error(1, "Invalid package name. Package name for '" + k + "' is not recognized as a valid package name")
						}
					} else {
						error(1, "Invalid ARG sent. Please provide ARG values of 'controller/connector' and 'domain'.")
					}


					if (domainArg == null) {
						error(1, "Missing valid domain value sent. Please try again.")
					}

					if (controllerArg == null && connectorArg == null) {
						connectorArg = connectorDir
					}
				}

				if (domainArg) {
					def entityManager = ctx.getBean('entityManagerFactory')
					Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();

					LinkedHashMap values = [:]
					for (EntityType tempEntityType : entities) {
						if (!domainFound) {
							if (tempEntityType.getJavaType().getCanonicalName() == domainArg) {

								domainFound = true

								// todo : should this be camelCase????
								realPackageName = tempEntityType.getJavaType().getCanonicalName() - (tempEntityType.getJavaType().getPackage().getName() + ".")
								realName = realPackageName.toLowerCase()

								Field[] fields = tempEntityType.getJavaType().getDeclaredFields()
								fields.each() {

									String attName = it.getName()
									Annotation anno = it.getAnnotation(javax.persistence.Column.class);
									LinkedHashMap constraints = [:]

									String keyType = null
									String reference = null
									if (attName != 'serialVersionUID') {
										Attribute att = tempEntityType.getDeclaredAttribute(attName)
										if (att.isAssociation()) {
											keyType = (['id', 'ID'].contains(attName)) ? 'PKEY' : 'FKEY'
											if (keyType == 'FKEY') {
												reference = att.getJavaType().getSimpleName()
											}
										}
									}

									if(attName != 'serialVersionUID') {
										variables.add("${attName}")
										if (anno != null) {

											constraints['nullable'] = anno.nullable()
											constraints['unique'] = anno.unique()
											if (keyType) {

												if (reference) {
													//FKEY
													values[attName] = ['key': keyType, 'reference': reference, 'type': it.getType().getCanonicalName(), 'constraints': constraints, 'description': '<put your description here>', 'mockData': '<put your mock data here>']
												} else {
													//PKEY
													values[attName] = ['key': keyType, 'type': it.getType().getCanonicalName(), 'constraints': constraints, 'description': '<put your description here>', 'mockData': '<put your mock data here>']
												}
											} else {
												values[attName] = ['type': it.getType().getCanonicalName(), 'constraints': constraints, 'description': '<put your description here>', 'mockData': '<put your mock data here>']
											}
										} else {
											values[attName] = ['type': it.getType().getCanonicalName(), 'constraints': null, 'description': '<put your description here>', 'mockData': '<put your mock data here>']
										}
									}
								}
							}
						}
					}

					// create attList
					data['realName'] = realName
					if(connectorArg) {
						println(values)
						data['attList'] = createAttList(values)
					}

					// todo : create method to check if controller exists
					// if exists, fill out 'URI' json
					// else template URI json using params from values
					String uris = createUriAtts(realName)
					data['uris'] = uris

					if (domainFound) {
						if (controllerArg) {
							createController(data)
						} else if (connectorArg) {
							createConnector(data)
						}
					} else {
						error(1, "Entity name '${domainArg}' not found. Please try again.")
					}
				}
			}
		}
	}

	private String createUriAtts(String controllerName){
		ArrayList ignoreList = [
				'setMetaClass',
				'getMetaClass',
				'handleRequest',
				'formatMap',
				'formatEntity',
				'convertModel',
				'parseResponseParams',
				'writeErrorResponse',
				'invokeMethod',
				'getProperty',
				'setProperty',
				'wait',
				'notify',
				'notifyAll',
				'getClass',
				'hashCode',
				'toString',
				'equals','wait',
				'getTraceService',
				'setTraceService'
		]

		// todo: create json String from 'variables' arraylist for use below
		String varString = createVarString(variables)

		String uris = ""
		LinkedHashMap<String, Object> cont = listableBeanFactory.getBeansWithAnnotation(org.springframework.stereotype.Controller.class)
		cont.each() { k, v ->
			if(k == controllerName){
				println(v.getClass())

				Method[] actions = v.getClass().getMethods()
				// get methods as 'actions'
				actions.each() { it4 ->
					if(!ignoreList.contains(it4.getName())) {
						String method = ""
						String req
						String resp
						Pattern listPattern = Pattern.compile("list|listBy")
						Pattern getPattern = Pattern.compile("get|getBy|show|showBy|enable")
						Pattern postPattern = Pattern.compile("create|make|generate|build|save|new")
						Pattern putPattern = Pattern.compile("edit|update")
						Pattern deletePattern = Pattern.compile("delete|deleteBy|disable|disableBy|destroy|kill|reset|resetBy")

						Matcher getm = getPattern.matcher(it4.getName())
						if (getm.find()) {
							method = 'GET'
							req = "[\"id\"]"
							resp = varString
						}

						Matcher listm = listPattern.matcher(it4.getName())
						if (listm.find()) {
							method = 'GET'
							resp = varString
						}

						if (method.isEmpty()) {
							Matcher postm = postPattern.matcher(it4.getName())
							if (postm.find()) {
								method = 'POST'
								req = varString
								resp = "[\"id\"]"
							}
						}

						if (method.isEmpty()) {
							Matcher putm = putPattern.matcher(it4.getName())
							if (putm.find()) {
								method = 'PUT'
								req = varString
								resp = "[\"id\"]"
							}
						}

						if (method.isEmpty()) {
							Matcher delm = deletePattern.matcher(it4.getName());
							if (delm.find()) {
								method = 'DELETE'
								req = "[\"id\"]"
								resp = "[\"id\"]"
							}
						}


String uri = """
\t\t\t\t\"${it4.getName()}\": {
\t\t\t\t\t\"METHOD\": "${method}",
\t\t\t\t\t\"DESCRIPTION\": \"Description for ${it4.getName()}\",
\t\t\t\t\t"ROLES\": {
\t\t\t\t\t\t"BATCH\": [\"ROLE_ADMIN\"]
\t\t\t\t\t},
\t\t\t\t\t\"REQUEST\": {
\t\t\t\t\t\t\"permitAll\": ${req}
\t\t\t\t\t},
\t\t\t\t\t\"RESPONSE\": {
\t\t\t\t\t\t\"permitAll\": ${resp}
\t\t\t\t\t}
\t\t\t\t},"""
						uris <<= uri
					}
				}
			}

		}
		return uris
	}

	private String createVarString(ArrayList variables){
		String varString = "["
		int inc = 1
		variables.each(){
			varString += "\"${it}\""
			if(inc!=variables.size()){
				varString += ","
				inc++
			}
		}
		varString += "]"
		return varString
	}

	private String createAttList(LinkedHashMap values){
		String json = ""
		int inc=1
		values.each() { k, v ->
json += """
\t\t"${k}": {"""
			if (v.keyType == 'PKEY') {
				json += "\t\t\t\"key\": \"${v.keyType}\","
			} else if (v.keyType == 'FKEY') {
json += """\t\t\t\t"key": "${v.keyType}",
\t\t\t\t"references": "","""
			}

json += """
\t\t\t"type": "${entityTypeConverter(v.type)}",
\t\t\t"description": \"\",
\t\t\t"mockData": \"\","""

			if(v.constraints){
json += """
\t\t\t\"constraints\": {\"order\":${inc},\"isNullable\":${v.constraints.nullable}, \"isUnique\":${v.constraints.unique}},"""
			}else{
json += """
\t\t\t\"constraints\": {\"order\":${inc}},"""
			}

json += """
\t\t},"""
			inc++
		}

		return json
	}

	private void createController(LinkedHashMap data){
		println("### creating controller...")

		// check to see if it exists
		Map<String, Object> controllers = listableBeanFactory.getBeansWithAnnotation(Controller.class)
		controllers.each(){ k, v ->
			if(!controllerFound) {
				if (v.getClass().getPackage().getName() == controllerArg) {
					controllerFound = true
					obj = v
					data[realName]['packageName'] = controllerArg
				}
			}
		}

		// check directory structure (in case this is FIRST controller)
		String controllerPath = controllerArg.replaceAll("\\.","/");
		String pathType
		if(dirExists("${System.getProperty('user.dir')}/src/main/groovy/${path}")){ pathType='groovy' }
		if(dirExists("${System.getProperty('user.dir')}/src/main/java/${path}")){ pathType='java' }
		if(!controllerFound && !pathType==null){
			error(1, "Sent controller class did not match any existing package using the 'Controller' annotation NOR directory structure. Please try again with the full package.")
		}

		//start scaffold process

		error(0, "")
	}

	private void createConnector(LinkedHashMap data){
		println("### creating connector...")
		println(System.getProperty('user.home'))

		String connectorPath = "${System.getProperty('user.home')}/${connectorDir}"
		if(!dirExists(connectorPath)){
			// need to create path
			error(1, "The 'iostateDir' in your 'beapi_api.yml' file is not porperly defined as the directory does not exist. Please check and try again.")
		}


		writeConnector("templates/Connector.json.template", "${System.getProperty('user.home')}/${connectorDir}/${realPackageName}.json", data)

		error(0, "")
	}

	private boolean dirExists(String path) {
		boolean exists = false
		//def ant = new AntBuilder()
		def file = new File(path)
		if (file.exists()) {
			exists = true
			return exists
		}
		return exists
	}

	private boolean fileExists(String path){
		def cfile = new File(path)
		if (cfile.exists()) {
			return true
		}
		return false
	}

	private void error(int i, String msg) {
		if (msg != "") {
			System.err << "${msg}"
		}
		System.exit i
	}


	void writeConnector(String inPath, String outPath, LinkedHashMap attribs){
		String starterDir = new File(getClass().protectionDomain.codeSource.location.path).path
		def starter = new File(starterDir)
		if (starter.isFile() && starter.name.endsWith("jar")) {
			JarFile jar = new JarFile(starter)
			JarEntry entry = jar.getEntry(inPath)

			InputStream inStream = jar.getInputStream(entry)

			OutputStream out = new FileOutputStream(outPath)
			int c
			while ((c = inStream.read()) != -1) {
				out.write(c)
			}
			inStream.close()
			out.close()
			jar.close()

			def templateFile = new File(outPath)
			def engine = new groovy.text.GStringTemplateEngine()
			def template = engine.createTemplate(templateFile).make(attribs)

			String controller = template.toString()

			BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))
			writer.write(controller)
			writer.close()
		}else{
			// throw error
			error(1, "Project is trying to use CLI but does not use 'spring-boot-starter-beapi'. Please include the starter to use this service.");
		}
	}



	/*
	void writeController(String inPath, String outPath, LinkedHashMap attribs){
		String pluginDir = new File(getClass().protectionDomain.codeSource.location.path).path
		def plugin = new File(pluginDir)
		try {
			if (plugin.isFile() && plugin.name.endsWith("jar")) {
				JarFile jar = new JarFile(plugin)
				JarEntry entry = jar.getEntry(inPath)
				InputStream inStream = jar.getInputStream(entry)
				OutputStream out = new FileOutputStream(outPath)
				int c
				while ((c = inStream.read()) != -1) {
					out.write(c)
				}
				inStream.close()
				out.close()
				jar.close()

				def templateFile = new File(outPath)
				def engine = new groovy.text.GStringTemplateEngine()
				def template = engine.createTemplate(templateFile).make(attribs)

				String controller = template.toString()

				BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))
				writer.write(controller)
				writer.close()
			}
		}catch(Exception e){
			println("Exception. Please submit error report :"+e)
		}
	}

	 */




	String entityTypeConverter(String type){
		switch(type){
			case 'char':
			case 'java.lang.Character':
				return 'Character'
				break
			case 'bool':
			case 'java.lang.Boolean':
				return 'Boolean'
				break
			case 'byte':
			case 'java.lang.Byte':
				return 'Byte'
				break
			case 'short':
			case 'java.lang.Short':
				return 'Short'
				break
			case 'int':
			case 'java.lang.Integer':
				return 'Integer'
				break
			case 'long':
			case 'java.lang.Long':
				return 'Long'
				break
			case 'float':
			case 'java.lang.Float':
				return 'Float'
				break
			case 'double':
			case 'java.lang.Double':
				return 'Double'
				break
			case 'java.math.BigInteger':
				return 'BigInteger'
				break
			case 'java.math.BigDecimal':
				return 'BigDecimal'
				break
			case 'java.sql.Timestamp':
				return 'Timestamp'
				break
			case 'java.sql.Date':
			case 'java.util.Date':
				return 'Date'
				break
			case 'java.util.Currency':
				return 'Currency'
				break
			case 'java.util.Locale':
				return 'Locale'
				break
			case 'java.util.TimeZone':
				return 'TimeZone'
				break
			case 'java.net.URL':
				return 'URL'
				break
			case 'java.util.UUID':
				return 'UUID'
				break
			case 'java.lang.String':
			default:
				return 'String'
				break
		}
	}
}
