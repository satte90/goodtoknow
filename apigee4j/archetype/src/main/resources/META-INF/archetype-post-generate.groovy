println "Running post generate script..."
def moduleDir = new File(request.getOutputDirectory()+"/"+request.getArtifactId())

// replace com.sample with the group and artifact ids
println "Renaming java files to have capital first"
moduleDir.eachDirRecurse() { dir ->
    dir.eachFileMatch(~/.*.java/) { file ->
        def newFileName = file.getName().substring(0, 1).toUpperCase() + file.getName().substring(1);
        println "new file name = " + newFileName
        println "parent = " + file.getParent()
        println "renaming to " + file.getParent() + "/" + newFileName
        file.renameTo file.getParent() + "/" + newFileName
    }
}