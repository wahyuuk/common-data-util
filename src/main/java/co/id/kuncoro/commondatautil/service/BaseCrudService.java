package co.id.kuncoro.commondatautil.service;

import co.id.kuncoro.commonapi.dto.CreateDto;
import co.id.kuncoro.commonapi.dto.DetailResponseDto;
import co.id.kuncoro.commonapi.dto.ParamsRequest;
import co.id.kuncoro.commonapi.dto.ResponseData;
import co.id.kuncoro.commonapi.dto.ResponseDto;
import co.id.kuncoro.commonapi.dto.UpdateDto;
import co.id.kuncoro.commondatautil.dto.PageResponse;
import co.id.kuncoro.commondatautil.model.BaseEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface BaseCrudService<E extends BaseEntity<K>, K, C extends CreateDto, U extends UpdateDto,
    R extends ResponseDto<K>, D extends DetailResponseDto<K>> {

  PageResponse<R> findAll(ParamsRequest params, HttpServletRequest servletRequest);

  ResponseData<D> findById(K id);

  ResponseData<D> create(C request);

  ResponseData<List<D>> create(List<C> requestList);

  ResponseData<D> update(K id, U request);

  ResponseData<D> patch(K id, U request);

  ResponseData<D> deleteById(K id);

  ResponseData<List<D>> deleteAllById(List<K> ids, HttpServletRequest servletRequest);

}
