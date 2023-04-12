
# TODOS:

1) Add swagger json file (see notes below)

2) Find all todos in generated project (src/main/*) / README.md and review them. 
   - If your Apigee integration follows some sort of standard it should hopefully be good to go as is
but likely it is not. For example the API may have differnet paths in different environments, in such case add environment specific properties.
The api may expose different versions for different endpoints, if so you may want to add additional clients and config for these.

3) (Optional) Feel free to add any other useful java utils / builders e.t.c.

4) Delete this file

## Notes about swagger generation:

Java models can (and should whenever possible) be generated from swagger.json file provided in Apigee. 
To do so run:
```
mvn clean install -Pupdate_api
```
This will attempt to download the swagger json file from api-market based on the data provided in generation of this apigee4j module. 
If this fails (may happen for wierdly setup apis) you can download the json file manually and put it in ```src/main/resources/swagger``` and call it 
```apigee4j-${api-name}_v${api-major-version}_${api-minor-version}``` (Note any patch version is omitted here, model *should* not be changed in a patch release)

Json is downloaded from AT environment but if you download it manually you can take it from SIT or prod as well as long as it matches your version.

## Finally. 

**Remove this file before pushing!** If you find this file in the git repo feel free to send a RTFM to the committer :D

*(Does not apply to apigee4j core repo.)*