refreshable-beans
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
===============

      1. Dynamic Rules Engine: Simple Rules engine could be created for example with rules engine running from startup and
      specify the rules at runtime (store the rules in the database) and the engine uses to process it. Using this
      approach only simple rules can be achieved and if complex rules are needed, the rules engine need to be modified.
      With this project approach, the new complex rules can be quiet easily created at runtime and registered.
      
      2. Changing different algorithms at runtime: The business logic needs to change to different algorithm at runtime, this
      could be achieved using this project approach.
      
      

Usage:
======

      This project uses Sprin MVC for exposing the rest endpoint to register / replace the groovy script at runtime.
      It provides the approach taken to create / change the algorithm at runtime.
      
      
      
