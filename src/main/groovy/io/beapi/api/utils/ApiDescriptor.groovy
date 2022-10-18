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
 */
package io.beapi.api.utils

import java.io.Serializable
import javax.validation.constraints.Size
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

/**
 *
 * Api Object used for caching all data associated with endpoint
 * @author Owen Rubel
 *
 * @see ApiCommLayer
 * @see BatchInterceptor
 * @see ChainInterceptor
 *
 */

class ApiDescriptor implements Serializable{

	boolean empty = false
	String defaultAction = ""
	//ArrayList testOrder
	//ArrayList deprecated
	String type
	//String handler

	//@NotNull
	String networkGrp

	//@NotNull
	//@Pattern(regexp = "GET|POST|PUT|DELETE", flags = Pattern.Flag.CASE_INSENSITIVE)
	String method
	LinkedHashSet pkeys
	LinkedHashSet fkeys
	Set keyList
	ArrayList roles
	ArrayList batchRoles
	ArrayList hookRoles

	//@NotNull
	//@Size(max = 200)
	String name

    LinkedHashMap<String,ParamsDescriptor> receives
	Set receivesKeys
    LinkedHashMap<String,ParamsDescriptor> returns
	Set returnsKeys
	LinkedHashMap<String,ArrayList> receivesList
	LinkedHashMap<String,ArrayList> returnsList
	LinkedHashMap cachedResult
	//LinkedHashMap stats

	ApiDescriptor(String networkGrp, String method, LinkedHashSet pkeys, LinkedHashSet fkeys, ArrayList roles,String name, LinkedHashMap receives, LinkedHashMap receivesList, LinkedHashMap returns, LinkedHashMap returnsList) {
		this.networkGrp = networkGrp
		this.method = method
		this.pkeys=pkeys
		this.fkeys=fkeys
		this.keyList = pkeys+fkeys
		this.roles=roles
		this.name=name
		this.receives=receives as LinkedHashMap
		this.receivesList=receivesList as LinkedHashMap
		this.receivesList.each(){ it->
			if(keyList.contains(it)){
				receivesKeys.add(it)
			}
		}
		this.returns=returns as LinkedHashMap
		this.returnsList=returnsList as LinkedHashMap
		this.returnsList.each(){ it->
			if(keyList.contains(it)){
				returnsKeys.add(it)
			}
		}
	}

	public String getMethod() {
		return this.method;
	}

	public String getNetworkGrp() {
		return this.networkGrp;
	}

	public Set getKeyList() {
		return this.keyList
	}

	public ArrayList getRoles() {
		return this.roles
	}

	public ArrayList getBatchRoles() {
		return this.batchRoles
	}

	public ArrayList getHookRoles() {
		return this.hookRoles
	}

	public String getName() {
		return this.name;
	}

	public boolean receivesRoleExists(String role){
		Set keys = this.receives.keySet()
		return keys.contains(role)
	}

	public LinkedHashMap getReceives() {
		return this.receives
	}

	public Set getReceivesKeys(){
		return this.receivesKeys
	}

	public LinkedHashMap getReceivesList() {
		return this.receivesList
	}

	public LinkedHashMap getReturns() {
		return this.returns
	}

	public Set getReturnsKeys(){
		return this.returnsKeys
	}

	public LinkedHashMap getReturnsList() {
		return this.returnsList
	}

	public LinkedHashMap getCachedResult() {
		return this.cachedResult
	}

	public LinkedHashMap toLinkedHashMap() {
		return [networkGrp: this.networkGrp, method: this.method, roles: this.roles, name: this.name, receives: this.receives, receivesList: this.receivesList, returns: this.returns, returnsList: this.returnsList]
	}

}
