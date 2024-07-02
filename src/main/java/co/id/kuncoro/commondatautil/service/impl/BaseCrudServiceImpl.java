package co.id.kuncoro.commondatautil.service.impl;

import co.id.kuncoro.commonapi.constant.ErrorCode;
import co.id.kuncoro.commonapi.dto.CreateDto;
import co.id.kuncoro.commonapi.dto.DetailResponseDto;
import co.id.kuncoro.commonapi.dto.ParamsRequest;
import co.id.kuncoro.commonapi.dto.ResponseData;
import co.id.kuncoro.commonapi.dto.ResponseDto;
import co.id.kuncoro.commonapi.dto.UpdateDto;
import co.id.kuncoro.commonapi.exception.ServiceException;
import co.id.kuncoro.commonapi.util.TracerUtils;
import co.id.kuncoro.commondatautil.dto.PageResponse;
import co.id.kuncoro.commondatautil.mapper.CrudMapper;
import co.id.kuncoro.commondatautil.model.BaseEntity;
import co.id.kuncoro.commondatautil.repository.BaseRepository;
import co.id.kuncoro.commondatautil.service.BaseCrudService;
import co.id.kuncoro.commondatautil.util.FilterUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public abstract class BaseCrudServiceImpl<E extends BaseEntity<K>, K, C extends CreateDto, U extends UpdateDto,
    R extends ResponseDto<K>, D extends DetailResponseDto<K>> implements BaseCrudService<E, K, C, U, R, D> {

  private final BaseRepository<E, K> repository;
  private final CrudMapper<E, K, C, U, R, D> mapper;
  private final TracerUtils tracerUtils;
  private final Class<E> entityClass;

  @Override
  public PageResponse<R> findAll(ParamsRequest params, HttpServletRequest servletRequest) {
    var filters = FilterUtils.getFilter(servletRequest, entityClass);
    var specification = FilterUtils.specification(filters, entityClass);
    var page = repository.findAll(specification, params.getPageRequest());
    var content = CollectionUtils.emptyIfNull(page.getContent())
        .stream()
        .map(mapper::response)
        .toList();
    return PageResponse.of(content, filters, params.getPageRequest(), page.getTotalElements());
  }

  @Override
  public ResponseData<D> findById(K id) {
    return repository.findById(id)
        .map(mapper::detail)
        .map(e -> ResponseData.ok(e, tracerUtils))
        .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND));
  }

  @Override
  public ResponseData<D> create(C request) {
    var entity = repository.save(mapper.create(request));
    return Optional.ofNullable(entity)
        .map(mapper::detail)
        .map(e -> ResponseData.ok(e, tracerUtils))
        .orElseThrow(() -> new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR));
  }

  @Override
  @Transactional
  public ResponseData<List<D>> create(List<C> requestList) {
    var entities = CollectionUtils.emptyIfNull(requestList)
        .stream()
        .map(mapper::create)
        .toList();
    var results = CollectionUtils.emptyIfNull(repository.saveAll(entities))
        .stream()
        .map(mapper::detail)
        .toList();
    return ResponseData.ok(results, tracerUtils);
  }

  @Override
  public ResponseData<D> update(K id, U request) {
    var existing = repository.findById(id)
        .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND));
    mapper.update(existing, request);
    repository.save(existing);
    return Optional.ofNullable(repository.save(existing))
        .map(mapper::detail)
        .map(e -> ResponseData.ok(e, tracerUtils))
        .orElseThrow(() -> new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR));
  }

  @Override
  public ResponseData<D> patch(K id, U request) {
    var existing = repository.findById(id)
        .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND));
    mapper.patch(existing, request);
    repository.save(existing);
    return Optional.ofNullable(repository.save(existing))
        .map(mapper::detail)
        .map(e -> ResponseData.ok(e, tracerUtils))
        .orElseThrow(() -> new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR));
  }

  @Override
  public ResponseData<D> deleteById(K id) {
    return repository.findById(id)
        .map(entity -> {
          repository.deleteById(id);
          return ResponseData.ok(mapper.detail(entity), tracerUtils);
        })
        .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND));
  }

  @Override
  public ResponseData<List<D>> deleteAllById(List<K> ids, HttpServletRequest servletRequest) {
    var existings = repository.findAllById(ids);
    if (existings.isEmpty()) {
      throw new ServiceException(ErrorCode.NOT_FOUND);
    }

    var results = existings.stream()
        .map(mapper::detail)
        .toList();
    return ResponseData.ok(results, tracerUtils);
  }


}
