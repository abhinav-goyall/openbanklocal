def readProperties()
{

	def properties_file_path = "${workspace}" + "@script/properties.yml"
	def property = readYaml file: properties_file_path
	env.APP_NAME = property.APP_NAME
        env.MS_NAME = property.MS_NAME
        env.BRANCH = property.BRANCH
        env.GIT_SOURCE_URL = property.GIT_SOURCE_URL
	env.GIT_CREDENTIALS = property.GIT_CREDENTIALS
        env.SONAR_HOST_URL = property.SONAR_HOST_URL
        env.CODE_QUALITY = property.CODE_QUALITY
        env.UNIT_TESTING = property.UNIT_TESTING
        env.CODE_COVERAGE = property.CODE_COVERAGE
        env.FUNCTIONAL_TESTING = property.FUNCTIONAL_TESTING
        env.SECURITY_TESTING = property.SECURITY_TESTING
	env.PERFORMANCE_TESTING = property.PERFORMANCE_TESTING
	env.TESTING = property.TESTING
	env.QA = property.QA
	env.PT = property.PT
	env.User = property.User
    env.DOCKER_REGISTRY = property.DOCKER_REGISTRY
	env.mailrecipient = property.mailrecipient
	
    
}



def FAILED_STAGE
podTemplate(cloud:'openshift',label: 'selenium', 
  containers: [
    containerTemplate(
      name: 'jnlp',
      image: 'cloudbees/jnlp-slave-with-java-build-tools',
      alwaysPullImage: true,
      args: '${computer.jnlpmac} ${computer.name}'
    )])
{
node 
{
   def MAVEN_HOME = tool "Maven_HOME"
   def JAVA_HOME = tool "JAVA_HOME"
   env.PATH="${env.PATH}:${MAVEN_HOME}/bin:${JAVA_HOME}/bin"
   try{
   stage('Checkout')
   {
       FAILED_STAGE=env.STAGE_NAME
       readProperties()
       checkout([$class: 'GitSCM', branches: [[name: "*/${BRANCH}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '${GIT_CREDENTIALS}', url: "${GIT_SOURCE_URL}"]]])
       sh 'oc set image --local=true -f Orchestration/deployment.yaml ${MS_NAME}=${DOCKER_REGISTRY}/openbank-dev-apps/${MS_NAME}:openbank-test-apps --dry-run -o yaml >> Orchestration/deployment-openbank-test.yaml'
sh 'oc set image --local=true -f Orchestration/deployment.yaml ${MS_NAME}=${DOCKER_REGISTRY}/openbank-dev-apps/${MS_NAME}:openbank-dev-apps --dry-run -o yaml >> Orchestration/deployment-openbank-dev.yaml'

   }

   stage('Initial Setup')
   {   
       FAILED_STAGE=env.STAGE_NAME
       sh 'mvn clean compile'
   }
   if(env.UNIT_TESTING == 'True')
   {
   	stage('Unit Testing')
   	{
        	
        	FAILED_STAGE=env.STAGE_NAME
        	sh 'mvn test'
   	}
   }
   if(env.CODE_COVERAGE == 'True')
   {
   	stage('Code Coverage')
   	{
		FAILED_STAGE=env.STAGE_NAME
		sh 'mvn package'
   	}
   }
   if(env.CODE_QUALITY == 'True')
   {
   	stage('Code Quality Analysis')
   	{
       		FAILED_STAGE=env.STAGE_NAME
       		sh 'mvn sonar:sonar -Dsonar.host.url="${SONAR_HOST_URL}"'
   	}
   }
  if(env.SECURITY_TESTING == 'True')
  {
	stage('Security Testing')
	{
		FAILED_STAGE=env.STAGE_NAME
		sh 'mvn findbugs:findbugs'
	}	
  }
   
   

   stage('Dev - Build Application')
   {
       FAILED_STAGE=env.STAGE_NAME
       sh 'oc config use-context openbank-dev-apps'
       sh 'oc config view'
       try{
       sh 'oc new-build "${GIT_SOURCE_URL}" --strategy=docker -n=openbank-dev-apps'
       }catch(e){
       sh 'oc start-build openbanklocal -n=openbank-dev-apps --wait=true'
       }
       
   }
   stage('Tagging Image for Dev')
   {
   
         openshiftTag(namespace: 'openbank-dev-apps', srcStream: '$MS_NAME', srcTag: 'latest', destStream: '$MS_NAME', destTag: 'openbank-dev-apps')
         
         /* FAILED_STAGE=env.STAGE_NAME
         sh'docker pull docker-registry.default.svc:5000/$APP_NAME-dev-apps/$MS_NAME'

	     sh'docker tag  docker-registry.default.svc:5000/$APP_NAME-dev-apps/$MS_NAME:latest ${DOCKER_REGISTRY}/$APP_NAME-dev-apps/$MS_NAME:dev-apps'

	     sh'docker push ${DOCKER_REGISTRY}/$APP_NAME-dev-apps/$MS_NAME:dev-apps' */
       
   }
   stage('Dev - Deploy Application')
   {   
       FAILED_STAGE=env.STAGE_NAME
       sh 'oc config use-context openbank-dev-apps'
       sh 'oc config view'
       sh 'oc apply -f Orchestration/deployment-openbank-dev.yaml -n=openbank-dev-apps'
       sh 'oc apply -f Orchestration/service.yaml -n=openbank-dev-apps'
       
   }
	
   stage('openbank-test - Tagging Image for Testing')
   {
   
   
        openshiftTag(namespace: 'openbank-dev-apps', srcStream: '$MS_NAME', srcTag: 'latest', destStream: '$MS_NAME', destTag: 'openbank-test-apps')
    
      //FAILED_STAGE=env.STAGE_NAME
     // sh'docker tag  ${DOCKER_REGISTRY}/$APP_NAME-dev-apps/$MS_NAME:dev-apps ${DOCKER_REGISTRY}/$APP_NAME-dev-apps/$MS_NAME:test-apps'

      // sh'docker push ${DOCKER_REGISTRY}/$APP_NAME-dev-apps/$MS_NAME:test-apps'


      
   }

	stage('openbank-test - Deploy Application')
	{
	
       FAILED_STAGE=env.STAGE_NAME
       sh 'oc config use-context openbank-test-apps'
       sh 'oc config view'	
	   sh 'oc apply -f Orchestration/deployment-openbank-test.yaml -n=openbank-test-apps'
    		   sh 'oc apply -f Orchestration/service.yaml -n=openbank-test-apps'
	}
	  
	node('selenium')
	{
	   
	stage('Integration Testing')
	{
	    FAILED_STAGE=env.STAGE_NAME
	    container('jnlp')
	    {
		 checkout([$class: 'GitSCM', branches: [[name: "*/${BRANCH}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '${GIT_CREDENTIALS}', url: "${GIT_SOURCE_URL}"]]])
		 sh 'mvn integration-test'
	    }
	 }
	 }


   

   

   
   
   
   }
	catch(e){
		echo "Pipeline has failed"
		emailext body: "${env.BUILD_URL} has result ${currentBuild.result} at stage ${FAILED_STAGE} with error" + e.toString(), subject: "Failure of pipeline: ${currentBuild.fullDisplayName}", to: "${mailrecipient}"
	    throw e
	
	}
	     
}
}	
