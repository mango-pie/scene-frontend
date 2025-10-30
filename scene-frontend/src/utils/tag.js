export const convertTagsToTree = (tagList) => {
    if (!Array.isArray(tagList)) {
        return [];
    }

    // 创建父标签映射表
    const parentTags = tagList.filter(tag => tag.isParent === 1);
    const childTags = tagList.filter(tag => tag.isParent === 0);

    // 构建树形结构
    return parentTags.map(parentTag => {
        // 找到该父标签的所有子标签
        const children = childTags
            .filter(childTag => childTag.parentId === parentTag.id)
            .map(childTag => ({
                text: childTag.tagName,
                id: childTag.tagName // 转换为字符串以匹配前端格式
            }));

        return {
            text: parentTag.tagName,
            children: children.length > 0 ? children : undefined
        };
    });
};

