# Octopus Task Manager 

As a person with multiple hobbies, I sometimes feel overwhelmed by the side projects I start but never finish. To mitigate this problem, I decided to create this small Java Swing app that helps me manage the tasks and ideas I have in mind and want to execute at some point. It suggests what to do next from all the projects I have based on priority, and also gives me information about which project has more tasks or which project I'm spending more time on.

## Features
- Dashboard
    - Shows the number of pending tasks per project.
    - Shows the Done tasks per project.
    - Shows the percentage of different priorities across all your projects.
    - Suggests what task to do next.
![Dashboard picture](https://github.com/nerydlg/OctopusTaskManager/blob/main/src/main/resources/md-images/dashboard.png?raw=true)

- Project tabs
    - Add a new project.
    - Create, Edit, and mark Tasks as done.
    - Create child tasks.
    - Move the tasks through your process.

![Project view](https://github.com/nerydlg/OctopusTaskManager/blob/main/src/main/resources/md-images/task_view.png?raw=true)

## Versions
I consider the current code a *beta* version; it works, but I haven't fully tested it.

## Change Log
| Version | Changes                                 |
|---------|-----------------------------------------|
| 0.1.1   | Project created with the features mentioned above |

## How to Install
> **NOTE:** This is a Java app so you will need to download also the [JRE](https://www.java.com/en/download)

For common users click [here](https://github.com/nerydlg/OctopusTaskManager/releases/download/0.1.1/TaskManager-0.1.1-SNAPSHOT.jar) and you will get the JAR file. 

This project requires Java and Maven to be compiled.
1. Clone the repo and do the `mvn clean package`
