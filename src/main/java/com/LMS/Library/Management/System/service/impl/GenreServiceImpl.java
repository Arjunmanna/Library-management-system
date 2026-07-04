package com.LMS.Library.Management.System.service.impl;

import com.LMS.Library.Management.System.exception.GenreException;
import com.LMS.Library.Management.System.mapper.GenreMapper;
import com.LMS.Library.Management.System.modal.Genre;
import com.LMS.Library.Management.System.payload.dto.GenreDTO;
import com.LMS.Library.Management.System.repository.GenreRepository;
import com.LMS.Library.Management.System.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Override
    public GenreDTO createGenre(GenreDTO genreDTO) {

        Genre genre = genreMapper.toEntity(genreDTO);
        Genre savedGenre = genreRepository.save(genre);

        GenreDTO dto = genreMapper.toDTO(savedGenre);

        return dto;
    }

    @Override
    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(genreMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GenreDTO getGenreById(Long genreId) throws GenreException {
        Genre genre = genreRepository.findById(genreId).orElseThrow(
                () -> new GenreException("genre not found")
        );
        return genreMapper.toDTO(genre);
    }

    @Override
    public GenreDTO updateGenre(Long genreId, GenreDTO genreDTO) throws GenreException {
        Genre existingGenre= genreRepository.findById(genreId).orElseThrow(
                ()-> new GenreException("genre not found")
        );

        genreMapper.updateEntityFromDTO(genreDTO, existingGenre);
        Genre updatedGenre = genreRepository.save(existingGenre);
        return genreMapper.toDTO(updatedGenre);
    }

    @Override
    public void deleteGenre(Long genreId) throws GenreException {
        Genre existingGenre= genreRepository.findById(genreId).orElseThrow(
                ()-> new GenreException("genre not found")
        );
        existingGenre.setActive(false);
        genreRepository.save(existingGenre);
    }

    @Override
    public void hardDeleteGenre(Long genreId) throws GenreException {
        Genre existingGenre= genreRepository.findById(genreId).orElseThrow(
                ()-> new GenreException("genre not found")
        );
        genreRepository.delete(existingGenre);
    }

    @Override
    public List<GenreDTO> getAllActiveGenresWithSubGenres() {
        List<Genre> topLevelGenres = genreRepository
                .findByParentGenreIsNullAndActiveTrueOrderByDisplayOrderAsc();
        return genreMapper.toDTOList(topLevelGenres);
    }

    @Override
    public List<GenreDTO> getTopLevelGenres() {
        List<Genre> topLevelGenres = genreRepository
                .findByParentGenreIsNullAndActiveTrueOrderByDisplayOrderAsc();
        return genreMapper.toDTOList(topLevelGenres);
    }

    @Override
    public Page<GenreDTO> searchGenres(String searchTerm, Pageable pageable) {
        return null;
    }

    @Override
    public long getTotalActiveGenres() {
        return genreRepository.countByActiveTrue();
    }

    @Override
    public long getBookCountByGenre(Long genreId) {
        return 0;
    }
}
