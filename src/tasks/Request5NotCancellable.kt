package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        .getOrgReposCall(req.org)
        .execute() // Executes request and blocks the current thread
        .also { logRepos(req, it) }
        .body() ?: listOf()

    val deferred = repos.map { repo ->
        GlobalScope.async {
            service
                .getRepoContributorsCall(req.org, repo.name)
                .execute() // Executes request and blocks the current thread
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }

    return deferred.awaitAll().flatten().aggregate()
}