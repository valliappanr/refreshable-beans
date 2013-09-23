Refreshable-beans
=================

Overview:
=========

    Spring dynamic language support is using ScriptFactory, ScriptFactoryPostProcessor. During App startup, app context 
    is parsed for dynamic language support, and parses the definition and registers ScriptFactoryPostProcessor for the 
    specified bean. ScriptFactoryPostProcessor does below things for dynamic language support with refreshability.
    
      1. Creates and registers a bean for ScriptFactory (based on script type - groovy, bsh)
      2. Creates a refreshable TargetSource (checks for any changes with respect to dynamic language script)
      3. Creates a Proxy object for the refreshable target source and registers for the actual bean (and the actual bean is 
      registered as prototype bean) along with the ScriptFactory bean definition.
      
      
      When a new request comes in for the scripted bean, the proxy checks for any changes to the script, if so creates a
      new bean which in turn loads the latest script using the ScriptFactory bean and uses that for any further request.
      
      The concept of this project is instead of registering the bean at startup, Using the same steps of 
      ScriptFactoryPostProcessor registers the bean at runtime using a secured rest endpoint. Advantages of doing this are
      are follows,
      
      1. Create new business logic at runtime without stopping the services
      2. Update the business logic created at runtime 


Usage Scenarios
---------------

      1. Dynamic Rules Engine: Simple Rules engine could be created for example with rules engine running from startup and
      specify the rules at runtime (store the rules in the database) and the engine uses to process it. Using this
      approach only simple rules can be achieved and if complex rules are needed, the rules engine need to be modified.
      With this project approach, the new complex rules can be quiet easily created at runtime and registered.
      
      2. Changing different algorithms at runtime: The business logic needs to change to different algorithm at runtime, this
      could be achieved using this project approach.
      
      

Usage:
------

      This project uses Sprin MVC for exposing the rest endpoint to register / replace the groovy script at runtime.
      It provides the approach taken to create / change the algorithm at runtime. 
      
      This project is tested with a sample groovy script Fibonacci.groovy - actual groovy script 
      registered at runtime. (Fibonacci-forloop.groovy / Fibonacci-recursive.groovy are used to replace 
      Fibonacci.groovy at runtime).
      
      It assumes that The groovy files are present at /tmp/scripts folder on the server and can be used to register at
      runtime.
      
      Below are the rest endpoints defined in the project,
      
      1. /dynamic-bean/status - provides the status of the system with list of available options
      2. /dynamic-bean/registry/{beanName} - registering the dynamic bean (beanName - name of the groovy file under
            /tmp/scripts) folder and registers a camel endpoint for the dynamic bean.
      3. /dynamic-bean/replace/{oldBean}/{newBean} - replace the oldBean script under /tmp/scripts  with newBean script
        under /tmp/scripts
      4. /send-message/{sequence} - Sends a message, which is routed to the camel endpoint, which inturn processed by
      the registered bean, sequence - to generate the fibonacci number for that sequence.
      
      
Testing:
--------

        1. Place two files Fibonacci-recursive.groovy, Fibonacci-forloop.groovy under /tmp/scripts. Copy 
            Fibonacci-forloop.groovy as Fibonacci.groovy under /tmp/scripts
        2. Using Restclient send a request to register /dynamic-bean/registry/Fibonacci, which registers the 
            for loop version of Fibonacci implementation and registers a camel endpoint for the bean (using activemq
            and the queue name is testqueue).
        3. Using Restclient send a request to  /dynamic-bean/send-message/10 - will provide the output inthe 
            server log as 
                        
                        Using For loop
                        55
        4. Using Restclient send a request to  /dynamic-bean/replace/Fibonacci/Fibonacci-recursive, will replace 
        the Fibonacci.groovy under /tmp/scripts to Fibonacci-recursive.groovy
        5. Using Restclient send a request to  /dynamic-bean/send-message/10 - will provide the output inthe 
            server log as 
                        
                        Using Recursive
                        55
