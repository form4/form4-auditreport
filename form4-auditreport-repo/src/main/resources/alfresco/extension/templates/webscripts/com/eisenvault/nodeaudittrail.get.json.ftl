<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	[
		<#list data as t>
		{
			"userName":"${t.userName}",
			"application":"${t.applicationName}",
			"method":"${t.applicationMethod}",
			"time":"${xmldate(t.date)}",
			"values":
			<#if t.auditValues??>
			{
				<#assign first=true>
				<#list t.auditValues?keys as k>
					<#if t.auditValues[k]??>
						<#if !first>,<#else><#assign first=false></#if>"${k}":<#assign value=t.auditValues[k]>"${value}"
						
					</#if>
				</#list>
			}
			<#else>null</#if>
		}<#if t_has_next>,</#if>
		</#list>
	]
}
</#escape>

<#-- renders an audit entry values -->
<#macro hashMap map simpleMode=false>
    <#assign index = 0 />
    <#list map?keys as key>
    <#if simpleMode>"<@parseValue value=key />":"<@parseValue value=map?values[index] />",
    <#else>
    <#assign value = map[key] />
    			<#if value?is_sequence>"<@parseValue value=key />":<#list value as element>"<@parseValue value=element />",</#list>  
    <#elseif value?is_hash>"<@parseValue value=key />":"<@hashMap map=value simpleMode=true />",
    			<#else>"<@parseValue value=key />":"<@parseValue value=value />",
    </#if>          
    </#if>          
    <#assign index = index + 1 />
   </#list>
</#macro>


<#-- renders an audit entry value -->
<#macro parseValue value="null">
    <#if value?is_number>"${value?c}"
    <#elseif value?is_boolean>"${value?string}"
    <#elseif value?is_date>"${xmldate(value)}"
    <#elseif value?is_string && value != "null">"${shortQName(value?string)}"
    </#if>
</#macro>

