define(["dojo/_base/declare",
        "alfresco/buttons/AlfButton",
        "alfresco/core/Core",
        "dojo/dom-class",
        "dojo/_base/array",
        "dojo/_base/lang",
        "dojo/_base/event"],
        function(declare, AlfButton, AlfCore, domClass, array, lang, event) {

   return declare([AlfButton, AlfCore], {

      onClick: function alfresco_buttons_AlfButton__onClick(evt) {
    	  window.history.back();
      }
   });
});