package dmit2015.service;

import dmit2015.entity.Movie;
import dmit2015.repository.MovieRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MovieService {

    @Inject
    private SecurityContext securityContext;

    private boolean isAuthenticated() {
        return !securityContext.getCallerPrincipal().getName().equalsIgnoreCase("anonymous");
    }

    private boolean hasContributorRole() {
        return securityContext.isCallerInRole("Sales");
    }

    private boolean hasAdministratorRole() {
        return securityContext.isCallerInRole("Administration" );
    }

    private String getCallerUsername() {
        return securityContext.getCallerPrincipal().getName();
    }


    @Inject
    private MovieRepository movieRepository;

    public void addMovie(Movie movie) {
        if (!isAuthenticated()) {
            throw new RuntimeException("Access denied. Anonymous users are not supported.");
        }
        if (!hasContributorRole()) {
            throw new RuntimeException("Access denied. Your role does not have permission to perform this task.");
        }
        movie.setUsername(getCallerUsername());

        LocalDateTime now = LocalDateTime.now();

        movie.setCreateTime(now);
        movie.setUpdateTime(now);

        movieRepository.insert(movie);
    }

    public void updateMovie(Movie movie) {
        if (!isAuthenticated()) {
            throw new RuntimeException("Access denied. Anonymous users are not supported.");
        }
        if (!hasContributorRole()) {
            throw new RuntimeException("Access denied. Your role does not have permission to perform this task.");
        }
        if (!movie.getUsername().equalsIgnoreCase(getCallerUsername())) {
            throw new RuntimeException("Access denied. You are not allowed to update another user data");
        }
        movie.setUpdateTime(LocalDateTime.now());

        movieRepository.update(movie);
    }

    public void deleteMovie(Movie movie) {
        if (!isAuthenticated()) {
            throw new RuntimeException("Access denied. Anonymous users are not supported.");
        }
        if (!hasAdministratorRole()) {
            throw new RuntimeException("Access denied. Your role does not have permission to perform this task.");
        }
        movieRepository.delete(movie);
    }

    public Optional<Movie> findMovieById(Long movieId) {
        return movieRepository.findOptionalById(movieId);
    }

    public List<Movie> getAllMovies() {
        if (!isAuthenticated()) {
            throw new RuntimeException("Access denied. Anonymous users are not supported.");
        }
        if (!(hasContributorRole() || hasAdministratorRole())) {
            throw new RuntimeException("Access denied. Your role does not have permission to perform this task.");
        }
        if (hasContributorRole()) {
            return movieRepository.findAllByUsername(getCallerUsername());
        }
        return movieRepository.findAll();
    }

    public long countMovies() {
        return movieRepository.count();
    }
}
