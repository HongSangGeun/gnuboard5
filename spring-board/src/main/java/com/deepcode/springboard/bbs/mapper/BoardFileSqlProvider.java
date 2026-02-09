package com.deepcode.springboard.bbs.mapper;

import java.util.Map;

public class BoardFileSqlProvider {

    public String insertFile(Map<String, Object> params) {
        return "insert into g5_board_file (" +
            "bo_table, wr_id, bf_no, bf_source, bf_file, bf_download, bf_content, bf_fileurl, bf_thumburl, bf_storage, " +
            "bf_filesize, bf_width, bf_height, bf_type, bf_datetime, bf_ocr_text" +
            ") values (" +
            "#{f.boTable}, #{f.wrId}, #{f.bfNo}, #{f.bfSource}, #{f.bfFile}, #{f.bfDownload}, #{f.bfContent}, #{f.bfFileurl}, #{f.bfThumburl}, #{f.bfStorage}, " +
            "#{f.bfFilesize}, #{f.bfWidth}, #{f.bfHeight}, #{f.bfType}, #{f.bfDatetime}, #{f.bfOcrText}" +
            ")";
    }

    public String incrementDownload(Map<String, Object> params) {
        return "update g5_board_file set bf_download = bf_download + 1 where bo_table = #{boTable} and wr_id = #{wrId} and bf_no = #{bfNo}";
    }

    public String deleteByPost(Map<String, Object> params) {
        return "delete from g5_board_file where bo_table = #{boTable} and wr_id = #{wrId}";
    }

    public String deleteByNo(Map<String, Object> params) {
        return "delete from g5_board_file where bo_table = #{boTable} and wr_id = #{wrId} and bf_no = #{bfNo}";
    }

    public String deleteByTable(Map<String, Object> params) {
        return "delete from g5_board_file where bo_table = #{boTable}";
    }

    public String updateBoTable(Map<String, Object> params) {
        return "update g5_board_file set bo_table = #{toBoTable} where bo_table = #{fromBoTable}";
    }
}
