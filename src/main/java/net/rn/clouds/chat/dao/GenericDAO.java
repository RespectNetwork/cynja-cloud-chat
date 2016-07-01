/**
 * 
 */
package net.rn.clouds.chat.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Noopur Pandey
 *
 */
public interface GenericDAO<T, ID extends Serializable> {

	T getById(ID id, boolean lock);

    T getById(ID id);

    T loadById(ID id);

    List<T> findAll();

    List<T> findByCriteria(Map criterias);

    String save(T entity);

    void update(T entity);

    void saveOrUpdate(T entity);

    void delete(T entity);

    void deleteById(ID id);
}
