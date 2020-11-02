package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgReposCall(req.org)
        .execute() // Executes request and blocks the current thread
        .also { logRepos(req, it) }
        .body() ?: listOf()

    val deferred = repos.map { repo ->
        async {
            service
                .getRepoContributorsCall(req.org, repo.name)
                .execute() // Executes request and blocks the current thread
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }

    deferred.awaitAll().flatten().aggregate()
}