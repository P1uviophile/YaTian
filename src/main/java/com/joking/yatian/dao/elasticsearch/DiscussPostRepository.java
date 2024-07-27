package com.joking.yatian.dao.elasticsearch;

import com.joking.yatian.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Joking7
 * @ClassName Elasticsearch
 * @description: DiscussPost Repository
 * @date 2024/7/28 上午1:11
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {

}
