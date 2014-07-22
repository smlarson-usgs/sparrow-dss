select sparrow_model_id         as model_id,
  (bound_north + bound_south) / 2 as midpoint_lat,
  (bound_east  + bound_west) / 2  AS midpoint_long,
  theme_name
FROM sparrow_model
WHERE is_public = 'T'
ORDER BY model_id;
