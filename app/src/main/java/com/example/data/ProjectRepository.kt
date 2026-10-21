package com.example.data

import com.example.model.Project
import com.example.model.SampleMediaLibrary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val projectDao: ProjectDao) {

    val allProjects: Flow<List<Project>> = projectDao.getAllProjects().map { entities ->
        entities.mapNotNull { entity ->
            runCatching { ProjectJsonConverter.fromJson(entity.dataJson) }.getOrNull()
        }
    }

    val allProjectEntities: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun saveProject(project: Project) {
        val json = ProjectJsonConverter.toJson(project)
        val entity = ProjectEntity(
            id = project.id,
            name = project.name,
            aspectRatio = project.aspectRatio.name,
            durationMs = project.durationMs,
            fps = project.fps,
            lastModified = System.currentTimeMillis(),
            dataJson = json
        )
        projectDao.insertProject(entity)
    }

    suspend fun getProjectById(id: String): Project? {
        val entity = projectDao.getProjectById(id) ?: return null
        return runCatching { ProjectJsonConverter.fromJson(entity.dataJson) }.getOrNull()
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }

    suspend fun ensureDefaultTemplates() {
        if (projectDao.getProjectCount() == 0) {
            for (template in SampleMediaLibrary.ALL_TEMPLATES) {
                saveProject(template)
            }
        }
    }
}
