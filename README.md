form4-auditreport
======================
This addon is a fork of [ev-aikau-auditreport](https://github.com/vipulswarup/ev-aikau-auditreport) and helps to view the audit data for a document on a separate page built using AIKAU framework.

## Differences and Enhancements

* Supporting Alfresco 5.2.x
* More control over who can see the report by configuration - see below
* Uses Alfresco SDK to 3.x

## Screenshots

![ev-aikau-auditreport](https://cloud.githubusercontent.com/assets/3936714/9897648/a189966e-5c7d-11e5-94ec-c608de6f5e89.png)

A custom Document Library Action and link on Document Details page are provided for viewing the audit report for a document.

![document library action](https://cloud.githubusercontent.com/assets/3936714/9900992/2e42caa8-5c97-11e5-9f7b-b3210bd2cbe8.png)

![document details action](https://cloud.githubusercontent.com/assets/3936714/9901010/5510babe-5c97-11e5-9dcc-2ba955e2a10c.png)  

# Configuration

This addon uses the default **alfresco-access** audit application. Make sure that you have below lines added to alfresco-global.properties:
  ```
  audit.enabled=true
  audit.alfresco-access.enabled=true
  audit.alfresco-access.sub-actions.enabled=true
  ```

## Access configuration (optional)

The following configuration shows the default permission configuration. It allows anybody show audit information.
  ```
  # Comma-separated list of alfresco groups that may use the audit report service. 
  # The 'ALL' setting allows all users to use it. The opposite to allow for nobody is an empty setting.
  # Example: ALFRESCO_ADMINISTRATORS,SITE_ADMINISTRATORS
  auditreport.allowedGroups=ALL

  # Comma-separated list of site groups that may use the audit report service. 
  # The 'ALL' setting allows all users to use it. The opposite to allow for nobody is an empty setting.
  # Example: SiteManager,SiteCollaborator
  auditreport.allowedSiteGroup=ALL

  # If a user needs to have write permission for the document, to show audit information of a document.
  # Value: false or true
  auditreport.writePermission=false
  ```  
**Notes:**
1. `auditreport.allowedGroups` and `auditreport.writePermission` criteria are `OR` combined. That means one of it must match. 
2. If `auditreport.writePermission` is set to `true` additionally a user need write permission on the document.

A common configuration may:
  ```
  auditreport.allowedGroups=ALFRESCO_ADMINISTRATORS,SITE_ADMINISTRATORS
  auditreport.allowedSiteGroup=SiteManager
  auditreport.writePermission=false
  ```
This ...
1. ... allows Alfresco or Site Administrators to get information of a document.
2. ... allows SiteManager of a site to access audit information of a document. 
3. ... other members of a Site can not access audit information of a document.

# Build & Install

1. Clone Repository 
2. run `mvn clean package`
3. Copy Amps to your Alfresco installation:
  - `form4-auditreport-repo` to amps folder
  - `form4-auditreport-share` to amps_share folder
4. Apply amps
  - simmply by running apply_amps.[sh|bat]
  - or for each application itself:
    - `java -jar alfresco-mmt.jar form4-auditreport-repo-XX.amp <webapps>/alfresco.war`
    - `java -jar alfresco-mmt.jar form4-auditreport-share-XX.amp <webapps>/share.war`
5. Assure correct file ownership to tomcat user for amps and generates .war files
5. Restart Tomcat
