<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/include/documentlist.lib.js">


var siteName = page.url.templateArgs.site;
var nodeRef = page.url.args.nodeRef;


var json = remote.connect("alfresco").get('/ev/hasPermissions?nodeRef=' + nodeRef.replace("://", "/"));
var jsonResult = eval('(' + json + ')');
if (json.status == 200 && jsonResult.result === "true")
{
	var jsonNode = AlfrescoUtil.getNodeDetails(nodeRef, null, {
	    actions: true
	});
	
	var auditListHeaderWidget = [
	{
	    name: "alfresco/lists/views/layouts/HeaderCell",
	    config: {
	       label: "User Name",
	       sortable: false
	    }
	 },
	 {
	    name: "alfresco/lists/views/layouts/HeaderCell",
	    config: {
	       label: "Application",
	       sortable: false
	    }
	 }                             
	];
	model.jsonModel = {
			services:[
			          "alfresco/services/CrudService",
			          "alfresco/services/NavigationService"
			],
		   widgets:[
					{
					    id: "SET_PAGE_TITLE",
					    name: "alfresco/header/SetTitle",
					    config: {
					       title: "Audit Trail"
					    }
					 },
		            {
		            	id: "SHARE_VERTICAL_LAYOUT",
		                name: "alfresco/layout/VerticalWidgets",
		                config:{
		                	widgets:[
								{
								    name: "alfresco/documentlibrary/AlfBreadcrumbTrail",
								    config: {
								       rootLabel: "Documents",
								       hide: false,
								       _currentNode: jsonNode.item,
								       currentPath: jsonNode.item.location.path,
								       lastBreadcrumbPublishTopic : "ALF_NAVIGATE_TO_PAGE",
								       additionalCssClasses: "breadcrumb-readonly"
								    }
								 },
								 {
									 name: "alfresco/layout/LeftAndRight",
									 config:{
										 style: {
				                              marginTop: "10px",
				                              marginBottom: "10px",
				                              marginLeft: "10px"
				                         },
				                         widgets:[
											{
											    name: "alfresco/renderers/FileType",
											    align: "left",
											    config: {
											       currentItem: jsonNode.item
											    }
											 },
											 {
												name: "alfresco/layout/VerticalWidgets",
												config:{
													align: "left",
													style: {
					                                       marginLeft: "20px"
					                                },
					                                widgets:[
					                                       {
					                                          name: "alfresco/layout/LeftAndRight",
					                                          config: {
					                                             style: {
					                                                marginBottom: "5px"
					                                             },
					                                             widgets: [
			                                                       {
			                                                    	   name:"alfresco/html/Label",
			                                                    	   align:"left",
			                                                    	   config:{
			                                                    		   style:{
			                                                    			   marginRight: "5px"
			                                                    		   },
			                                                    		   label:"Audit Trail for: "
			                                                    	   }
			                                                       },
					                                                {
					                                                   name: "alfresco/renderers/PropertyLink",
					                                                   align: "left",
					                                                   config: {
					                                                      propertyToRender: "node.properties.cm:name",
					                                                      postParam: "prop_cm_name",
					                                                      currentItem: jsonNode.item,
					                                                      renderSize: "large"
					                                                   }
					                                                },
					                                                {
					                                                   name: "alfresco/renderers/Separator",
					                                                   align: "left"
					                                                },
					                                                {
					                                                   name: "alfresco/renderers/Version",
					                                                   align: "left",
					                                                   config: {
					                                                      currentItem: jsonNode.item,
					                                                      style:{
			                                                    			   marginLeft: "5px"
			                                                    		   },
					                                                   }
					                                                }
					                                             ]
					                                          }
					                                       },
					                                       {
					                                           name: "alfresco/layout/LeftAndRight",
					                                           config: {
					                                              widgets: [
					                                                 {
					                                                    name: "alfresco/renderers/Date",
					                                                    align: "left",
					                                                    config: {
					                                                       currentItem: jsonNode.item
					                                                    }
					                                                 }
					                                              ]
					                                           }
					                                       }
					                                ]
												}
											 },
											 {
												 name:"eisenvault/widgets/BackButton",
												 align: "right",
												 config:{
													 style:{
														 marginLeft: "20px",
														 marginRight: "20px"
													 },
													 label: "Go Back"
												 }
											 }
				                         ]
									 }
								 }
		                	]
		                }
		            },
		            addTableWidget()
		   ]
	};
}
function addTableWidget(){
	return {
       name: "alfresco/layout/ClassicWindow",
       config: {
    	   style:{
      		 marginLeft: "10px",
      		 marginRight: "10px"
      	 },
          title: "Audit Data : ",
          refreshCurrentItem: true,
          widgets: [
            {
              name: "alfresco/layout/VerticalWidgets",
	              config: {
	                widgets:[
	                         createTable()
	                ]
	              }
	            }
	          ]
	       }
		};
	}

function createTable(){
	return{
		name: "alfresco/lists/AlfList",
		config:{
			loadDataPublishTopic: "ALF_CRUD_GET_ALL",
			loadDataPublishPayload:{
				url: "ev/nodeaudittrail?nodeRef="+nodeRef
			},
			itemsProperty: "data",
			widgets:[
			         {
			        	 name:"alfresco/lists/views/AlfListView",
			        	 config:{
			        		 additionalCssClasses: "bordered",
			        		 widgetsForHeader: [
			                                    {
			                                        name: "alfresco/documentlibrary/views/layouts/HeaderCell",
			                                        config: {
			                                            id: "userNameTableHeader",
			                                            label: "User Name"
			                                        }
			                                    },
			                                    {
			                                        name: "alfresco/documentlibrary/views/layouts/HeaderCell",
			                                        config: {
			                                            id: "applicationTableHeader",
			                                            label: "Application"
			                                        }
			                                    },
			                                    {
			                                        name: "alfresco/documentlibrary/views/layouts/HeaderCell",
			                                        config: {
			                                            id: "methodTableHeader",
			                                            label: "Method"
			                                        }
			                                    },
			                                    {
			                                        name: "alfresco/documentlibrary/views/layouts/HeaderCell",
			                                        config: {
			                                            id: "timeTableHeader",
			                                            label: "Time"
			                                        }
			                                    },
			                                    {
			                                        name: "alfresco/documentlibrary/views/layouts/HeaderCell",
			                                        config: {
			                                            id: "auditEntryValuesTableHeader",
			                                            label: "Audit Entry Values"
			                                        }
			                                    }
			                                ],
			        		 widgets:[
			        		          {
			        		        	  name: "alfresco/lists/views/layouts/Row",
			        		        	  config:{
			        		        		  widgets:[
			        		        		           {
			        		        		        	   name: "alfresco/lists/views/layouts/Cell",
			        		        		        	   config:{
			        		        		        		   additionalCssClasses: "mediumpad",
			        		        		        		   widgets:[
			        		        		        		            {
			        		        		        		            	name:"alfresco/renderers/Property",
			        		        		        		            	config:{
			        		        		        		            		propertyToRender:"userName"
			        		        		        		            	}
			        		        		        		            }
			        		        		        		   ]
			        		        		        	   }
			        		        		           },
			        		        		           {
			        		        		        	   name: "alfresco/lists/views/layouts/Cell",
			        		        		        	   config:{
			        		        		        		   additionalCssClasses: "mediumpad",
			        		        		        		   widgets:[
			        		        		        		            {
			        		        		        		            	name:"alfresco/renderers/Property",
			        		        		        		            	config:{
			        		        		        		            		propertyToRender:"application"
			        		        		        		            	}
			        		        		        		            }
			        		        		        		   ]
			        		        		        	   }
			        		        		           },
			        		        		           {
			        		        		        	   name: "alfresco/lists/views/layouts/Cell",
			        		        		        	   config:{
			        		        		        		   additionalCssClasses: "mediumpad",
			        		        		        		   widgets:[
			        		        		        		            {
			        		        		        		            	name:"alfresco/renderers/Property",
			        		        		        		            	config:{
			        		        		        		            		propertyToRender:"method"
			        		        		        		            	}
			        		        		        		            }
			        		        		        		   ]
			        		        		        	   }
			        		        		           },
			        		        		           {
			        		        		        	   name: "alfresco/lists/views/layouts/Cell",
			        		        		        	   config:{
			        		        		        		   additionalCssClasses: "mediumpad",
			        		        		        		   widgets:[
			        		        		        		            {
			        		        		        		            	name:"eisenvault/widgets/Property",
			        		        		        		            	config:{
			        		        		        		            		propertyToRender: "time"
			        		        		        		            	}
			        		        		        		            }
			        		        		        		   ]
			        		        		        	   }
			        		        		           },
			        		        		           {
			        		        		        	   name: "alfresco/lists/views/layouts/Cell",
			        		        		        	   config:{
			        		        		        		   additionalCssClasses: "mediumpad",
			        		        		        		   widgets:[
			        		        		        		            {
			        		        		        		            	name:"eisenvault/widgets/Property",
			        		        		        		            	config:{
			        		        		        		            		propertyToRender:"values"
			        		        		        		            	}
			        		        		        		            }
			        		        		        		   ]
			        		        		        	   }
			        		        		           }
			        		        		  ]
			        		        	  }
			        		          }
			        		 ]
			        	 }
			         }
			]
		}
		
	}
}
