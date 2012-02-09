package com.xuyuan.pager_generic;import java.io.Serializable;import java.sql.SQLException;import java.util.List;import javax.annotation.Resource;import org.hibernate.Criteria;import org.hibernate.HibernateException;import org.hibernate.Query;import org.hibernate.Session;import org.hibernate.SessionFactory;import org.hibernate.criterion.Criterion;import org.hibernate.criterion.Order;import org.hibernate.criterion.Restrictions;import org.springframework.orm.hibernate3.HibernateCallback;import org.springframework.orm.hibernate3.support.HibernateDaoSupport;import org.springframework.stereotype.Repository;import com.xuyuan.pager_generic.model.PageModel;/** *  * @author http://www.blogjava.net/rongxh7/archive/2009/05/19/271462.html * @param <T> 实体对象 * @param <PK> 主键(实体的ID) * 其他参数说明: * Object... params				参数数组,可有0项目或多项目,代替Hql中的"?"号  * Class<T> entityClass 		实体类名 * Criterion... criterions		查询条件,可为0项或任意多项目 *  * 查询以下几种 * (Class<T> entityClass,Criterion...)				多个查询条件,构造Criterion...可变数组 * (Class<T> entityClass,propertyName,Object value)	单个查询条件 * (hql,Object... params)							HQL方式,Object...也是查询条件可变数组 *  * 返回结果集又分为:List<T> 或者PageModel<T> */@Repository(value="entityDao")public class EntityDaoSupport<T,PK extends Serializable> extends HibernateDaoSupport implements EntityDao<T,PK>{	@Resource(name="sessionFactory")        public void setSuperSessionFactory(SessionFactory sessionFactory){        super.setSessionFactory(sessionFactory);    }	    /**     * 保存实体:包括添加和修改.传入实体对象.     */    public void saveOrUpdate(T t){        getHibernateTemplate().saveOrUpdate(t);    }        /**     * 更新实体:可用于添加、修改、删除操作.传入实体对象     */    @SuppressWarnings("unchecked")	public void update(final String hql,final Object... params){        getHibernateTemplate().execute(new HibernateCallback(){            public Object doInHibernate(Session session) throws HibernateException, SQLException {                Query query = session.createQuery(hql);                for(int i=0; i<params.length; i++){                    query.setParameter(i, params[i]);                }                query.executeUpdate();                return null;            }        });    }        /**     * 删除实体:传入实体对象     */    public void delete(T t){        getHibernateTemplate().delete(t);    }        /**     * 删除实体:根据id删除记录.     */    public void delete(Class<T> entityClass,PK id){        getHibernateTemplate().delete(get(entityClass,id));    }        /**     * 单查实体     */    @SuppressWarnings("unchecked")    public T get(Class<T> entityClass,PK id){        return (T)getHibernateTemplate().get(entityClass, id);    }        /**     * 查询全部记录列表     * @param entityClass 	实体类名     * @param propertyName 	排序的参照属性.如果是多个属性排序呢?     * @param isAsc 		排序方式     * @param criterions 	查询条件,可为0项或任意多项目     * @return 记录List集     */    public List<T> findAll(final Class<T> entityClass,final String propertyName,final boolean isAsc,final Criterion... criterions){        return findByCriteria(entityClass, propertyName, isAsc, 0, 0, criterions);    }        /**     * 查询列表[List实体类记录集]     * @param entityClass 	实体类名     * @param propertyName 	排序的参照属性     * @param isAsc 		排序方式,true表示升序,false表示降序,当propertyName赋值为null时,此参数无效     * @param firstResult 	开始记录序号	==offset     * @param maxResults 	最大记录数	==pageSize     * @param criterions 	查询条件,可有0项或任意多项目     */    @SuppressWarnings("unchecked")    public List<T> findByCriteria(final Class<T> entityClass,final String propertyName,final boolean isAsc,     	final int firstResult,final int maxResults,final Criterion... criterions){        List<T> list = (List<T>)getHibernateTemplate().execute(new HibernateCallback(){            public Object doInHibernate(Session session) throws HibernateException, SQLException {            	//查询条件的做法: Criterion -> Criteria -> Criteria.add(Criterion)                Criteria criteria = session.createCriteria(entityClass);                //按属性条件查询                for(Criterion criterion : criterions){                    criteria.add(criterion);                }                //按某个属性排序                if(null != propertyName){                    if(isAsc){                        criteria.addOrder(Order.asc(propertyName));                    }else{                        criteria.addOrder(Order.desc(propertyName));                    }                }                //用于分页查询.如果maxResults=0,没有以下操作,则表示查询全部记录.                if(maxResults != 0){                    criteria.setFirstResult(firstResult);                    criteria.setMaxResults(maxResults);                }                List<T> list = criteria.list();                return list;            }        });        return list;    }        /**     * 查询总记录数.     * 传递Criterion...的原因是:在列表页面会根据条件查询,那么此时查询总数时也要带上和查询列表一样的查询条件.     * 没有像上面一样带上分页信息的原因是:查询总数是查询所有记录条数.不能加查询条件,否则只能查到一页.     */    @SuppressWarnings("unchecked")    public int findCountsByCriteria(final Class<T> entityClass,final Criterion... criterions){			int totalCounts = (Integer)getHibernateTemplate().execute(new HibernateCallback(){            public Object doInHibernate(Session session) throws HibernateException, SQLException {                Criteria criteria = session.createCriteria(entityClass);                for(Criterion criterion : criterions){                    criteria.add(criterion);                }                int totalCounts = criteria.list().size();                return totalCounts;            }        });        return totalCounts;    }            /**     * 分页查询     * 实际上是对findByCriteria和findCountsByCriteria两个函数的封装.     * @param entityClass 	实体类名     * @param propertyName 	排序参照属性     * @param isAsc 		排序方式,true表示升序,false表示降序,当propertyName赋值为null时,此参数无效     * @param firstResult 	开始记录序号     * @param maxResults 	最大记录数     * @param criterions 	查询条件,可为0项或任意多项目     * @return 封装List和totalCounts的Pager对象     */    @SuppressWarnings("unchecked")    public PageModel<T> findForPager(final Class<T> entityClass,final String propertyName,final boolean isAsc,final int firstResult,final int maxResults,final Criterion... criterions){    	List<T> entityList = findByCriteria(entityClass, propertyName, isAsc, firstResult, maxResults, criterions);        int totalCounts = findCountsByCriteria(entityClass, criterions);                PageModel pager = new PageModel();        pager.setTotal(totalCounts);        pager.setList(entityList);        return pager;    }        /**     * 根据属性值查询列表     * 查询全部记录列表findAll有一个参数是查询条件.因此根据条件查询列表,只要构造好查询条件即可.     * 该查询仅针对单个字段,并且是等于查询.     * @param entityClass 实体类名     * @param propertyName 属性名     * @param value 属性值     * @return List列表     */    public List<T> findByProperty(Class<T> entityClass,String propertyName,Object value){        Criterion criterion = Restrictions.eq(propertyName, value);        List<T> list = findAll(entityClass, null, true, criterion);        return list;    }        /**     * 根据属性值查询单个实体对象     */    @SuppressWarnings("unchecked")    public T findUniqueByProperty(final Class<T> entityClass,final String propertyName,final Object value){        T t = (T)getHibernateTemplate().execute(new HibernateCallback(){            public Object doInHibernate(Session session) throws HibernateException, SQLException {            	//一句话搞定.下面是模仿findByCriteria的写法: Criterion -> Criteria -> Criteria.add(Criterion)            	//Criteria criteria = session.createCriteria(entityClass).add(Restrictions.eq(propertyName, value));            	Criterion criterion = Restrictions.eq(propertyName, value);            	Criteria criteria = session.createCriteria(entityClass);            	criteria.add(criterion);                T t = (T)criteria.uniqueResult();                return t;            }        });        return t;    }        /**     * 根据属性值查询实体是否存在     * @return 存在则返回true,不存在则返回false     */    @SuppressWarnings("unchecked")    public boolean isPropertyExist(final Class<T> entityClass,final String propertyName,final Object value){        boolean isExist = (Boolean)getHibernateTemplate().execute(new HibernateCallback(){            public Object doInHibernate(Session session) throws HibernateException, SQLException {                Criteria criteria = session.createCriteria(entityClass).add(Restrictions.eq(propertyName, value));                boolean isEmpty = criteria.list().isEmpty();                return ! isEmpty;            }        });        return isExist;    }        /**     * HQL查询语句根据条件查询单个记录[唯一实体],     * 如果查询的结果集不唯一,则抛异常. 返回null则表示不存在配置的实体     * 可用于登录验证时，根据用户名、密码等信息查询用户     */    @SuppressWarnings("unchecked")    public T findUniqueByHql(final String hql, final Object... params){        T t = (T)getHibernateTemplate().execute(new HibernateCallback(){            public Object doInHibernate(Session session) throws HibernateException, SQLException {            	//和update方法一样,只不过update最后的操作是:query.executeUpdate();.这里最后返回的是单个实体对象.                Query query = session.createQuery(hql);                for(int i=0; i<params.length; i++){                    query.setParameter(i, params[i]);                }                T t = (T)query.uniqueResult();                return t;            }        });        return t;    }        /**     * 按HQL条件查询列表     * @param hql 查询语句,支持连接查询和多条件查询     * @param params 参数数组,代替hql中的"?"号     * @return 结果集List     */    @SuppressWarnings("unchecked")    public List<T> findByHql(String hql,Object... params){        List list = getHibernateTemplate().find(hql, params);        return list;    }        /**     * 按HQL分页查询     * @param firstResult 开始记录号     * @param maxResults 最大记录数     * @param hql 查询语句,支持连接查询和多条件查询     * @param params 参数数组,代替餐hql中的"?"号     * @return 封装List和total的Pager对象     */    @SuppressWarnings("unchecked")    public PageModel<T> findForPagerByHql(final int firstResult, final int maxResults, final String hql, final Object... params){        PageModel<T> pager = (PageModel<T>)getHibernateTemplate().execute(new HibernateCallback(){            public Object doInHibernate(Session session) throws HibernateException, SQLException {                Query query = session.createQuery(hql);                for(int position = 0; position < params.length; position ++){                    query.setParameter(position, params[position]);                }                int totalCounts = query.list().size();    //总记录数                //用于分页查询                if(maxResults > 0){                    query.setFirstResult(firstResult);                    query.setMaxResults(maxResults);                }                List<T> list = query.list();                PageModel<T> pager = new PageModel<T>();                pager.setList(list);                pager.setTotal(totalCounts);                return pager;            }        });        return pager;    }}