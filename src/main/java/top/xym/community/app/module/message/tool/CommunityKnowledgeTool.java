package top.xym.community.app.module.message.tool;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommunityKnowledgeTool {

    private final VectorStore vectorStore;

    public CommunityKnowledgeTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 社区知识库查询（政策、公告、办事指南）
     * @param query 用户问题
     * @return 知识库答案
     */
    @Tool(description = "查询社区知识库，包含社区政策、通知、办事指南等信息")
    public String queryKnowledge(String query) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3)
                        .build()
        );

        if (docs.isEmpty()) {
            return "暂无相关知识库信息";
        }

        StringBuilder sb = new StringBuilder("根据社区知识库为你解答：\n");
        for (Document doc : docs) {
            sb.append(doc.getText()).append("\n");
        }
        return sb.toString();
    }
}