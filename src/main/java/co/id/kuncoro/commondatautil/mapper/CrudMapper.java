package co.id.kuncoro.commondatautil.mapper;

import co.id.kuncoro.commonapi.dto.CreateDto;
import co.id.kuncoro.commonapi.dto.DetailResponseDto;
import co.id.kuncoro.commonapi.dto.ResponseDto;
import co.id.kuncoro.commonapi.dto.UpdateDto;
import co.id.kuncoro.commondatautil.model.BaseEntity;
import java.lang.reflect.Field;
import java.util.Objects;
import org.apache.commons.lang3.reflect.FieldUtils;

public interface CrudMapper<E extends BaseEntity<K>, K, C extends CreateDto, U extends UpdateDto,
    R extends ResponseDto<K>, D extends DetailResponseDto<K>> {

  E create(C request);

  D detail(E entity);

  void update(E entity, U request);

  R response(E entity);

  default E patch(E entity, U request) {
    var requestFields = FieldUtils.getAllFieldsList(request.getClass());
    for (Field field : requestFields) {
      var entityField = FieldUtils.getField(entity.getClass(), field.getName(), true);
      var requestValue = getEntityValue(request, field.getName());
      if (Objects.equals(field.getType(), entityField.getType()) && requestValue != null) {
        setFieldValue(entity, field.getName(), requestValue);
      }
    }

    return entity;
  }

  private Object getEntityValue(U request, String fieldName) {
    try {
      return FieldUtils.readField(request.getClass(), fieldName, true);
    } catch (IllegalAccessException | IllegalArgumentException e) {
      return null;
    }
  }

  private boolean setFieldValue(E entity, String fieldName, Object fieldValue) {
    try {
      FieldUtils.writeDeclaredField(entity, fieldName, fieldValue, true);
      return true;
    } catch (IllegalAccessException e) {
      return false;
    }
  }

}
