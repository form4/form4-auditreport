define(["dojo/_base/declare",
        "alfresco/renderers/Property", 
        "alfresco/core/ObjectTypeUtils",
        "alfresco/core/TemporalUtils"], 
        function(declare, Property, ObjectTypeUtils,TemporalUtils) {

   return declare([Property], {
       amIJSONString: function alfresco_renderers_Property__amIJSONString(property) {
          var jsonObject;
          try {
             jsonObject = JSON.parse(property);
          } catch (e) {
             return property;
          }
          return jsonObject;
       },
       /**
       * @instance
       * @param {string} property The name of the property to render
       */
      getRenderedProperty: function alfresco_renderers_Property__getRenderedProperty(property) {
         /*jshint maxcomplexity:false*/
         var value = "";
         if (property === null || typeof property === "undefined") {
            // No action required if a property isn't supplied
         } else if (ObjectTypeUtils.isString(property)) {
            //value = this.encodeHTML(property);
        	 value = this.formatDate(property,'dddd, d mmmm, yyyy \'at\' h:MM TT');
         } else if (ObjectTypeUtils.isArray(property)) {
            value = property.length;
         } else if (ObjectTypeUtils.isBoolean(property)) {
            value = property;
         } else if (ObjectTypeUtils.isNumber(property)) {
            value = property;
         } else if (ObjectTypeUtils.isObject(property)) {
            value = "<ul>";
            for (var element in property) {
                 if (property.hasOwnProperty(element)) {
                    value += "<li><strong>"+this.encodeHTML(element)+"</strong> = "+this.getRenderedProperty(this.amIJSONString(property[element]))+"</li>";
                 }
            }
            value += "</ul>";
         }
         return value;
      }
   });
});