package co.id.kuncoro.commondatautil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BaseRepository<E, K> extends JpaRepository<E, K>, JpaSpecificationExecutor<E> {

}
