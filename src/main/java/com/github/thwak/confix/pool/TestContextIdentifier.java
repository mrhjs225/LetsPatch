package com.github.thwak.confix.pool;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.github.thwak.confix.tree.Node;
import com.github.thwak.confix.tree.TreeUtils;
import com.github.thwak.confix.util.IOUtils;

import script.model.EditOp;
import tree.Tree;
import tree.TreeNode;

public class TestContextIdentifier extends ContextIdentifier {

	private static final long serialVersionUID = -8352611691723991826L;
	private int jsonNodeId = 0;
	JSONObject jsonObject = new JSONObject();
	JSONArray nodeArray = new JSONArray();
	JSONArray edgeArray = new JSONArray();
	ArrayList<ASTNode> contextNodeList = new ArrayList<>();

	@Override
	public Context getContext(EditOp op, File aFile, File bFile, Change c, Tree beforeTree) {
		ArrayList<String> leftNameList = new ArrayList<>();
		ArrayList<String> rightNameList = new ArrayList<>();

		System.out.println("=====================================================================");
		System.out.println("Filepath:" + aFile.getAbsolutePath());

		if(op.getType().equals(Change.INSERT)){
			StringBuffer sb = new StringBuffer();
			sb.append("P:");
			TreeNode parent = null;
			parent = op.getNode().getParent();
			if(parent.getType() == ASTNode.BLOCK && parent.getParent() != null)
				parent = parent.getParent();
			if(parent != null){
				if(parent.getMatched() != null){
					parent = parent.getMatched();
				}
				// extract the var. name and M.I in context
				// System.out.println("parentCode:" + parent.getASTNode());
				
				sb.append(TreeUtils.getTypeName(parent.getType()));
				StructuralPropertyDescriptor desc = null;
				ASTNode astNode = op.getNode().getASTNode();
				if(astNode.getParent().getNodeType() == ASTNode.EXPRESSION_STATEMENT)
					desc = astNode.getParent().getLocationInParent();
				else
					desc = astNode.getLocationInParent();
				if(desc != null) {
					sb.append(TreeUtils.SYM_OPEN);
					sb.append(desc.getId());
					sb.append(TreeUtils.SYM_CLOSE);
				}
			}
			
			TreeNode node = op.getNode();
			TreeNode left = node.getLeft();
			TreeNode right = node.getRight();
			while(right != null && !right.isMatched())
				right = right.getRight();
			//Get hash of old nodes as contexts.
			if(left != null && left.isMatched())
				left = left.getMatched();
			if(right != null && right.isMatched())
				right = right.getMatched();
			sb.append(",L:");
			if(left != null) {
				addNodeType(left, sb);
				extractNameinContext(left, leftNameList);
				System.out.println("left:" + left.getASTNode());
			}
			sb.append(",R:");
			if(right != null) {
				addNodeType(right, sb);
				extractNameinContext(right, rightNameList);
				
				System.out.println("right:" + right.getASTNode());
			}

			// extract procedure context info
			ASTParser afterFileParser = ASTParser.newParser(AST.JLS8);
			ASTParser beforeFileParser = ASTParser.newParser(AST.JLS8);
			String line = "";
			String afterFileLine = IOUtils.readFile(aFile);
			String beforeFileLine = IOUtils.readFile(bFile);

			afterFileParser.setSource(afterFileLine.toCharArray());
			beforeFileParser.setSource(beforeFileLine.toCharArray());
			afterFileParser.setKind(ASTParser.K_COMPILATION_UNIT);
			beforeFileParser.setKind(ASTParser.K_COMPILATION_UNIT);
			CompilationUnit afterCu = (CompilationUnit) afterFileParser.createAST(null);
			CompilationUnit beforeCu = (CompilationUnit) beforeFileParser.createAST(null);
			
			// check the normalizing --> success!
			

			// if (right != null) {
			// 	Node tempNode = Converter.convert(right);
			// 	TreeUtils.normalize(manager, tempNode, false);
			// 	tempNode.normalized = false;
			// 	System.out.println("after right normalize: " + Converter.getNormalizedCode(tempNode, IOUtils.readFile(bFile)));
			// }


			// Extract AST to json
			try {
				BufferedWriter bufWriter = new BufferedWriter(new FileWriter(new File(aFile.getAbsolutePath().substring(0, aFile.getAbsolutePath().length()-4) + "txt")));
				JSONObject jsonNode = new JSONObject();
				JSONObject jsonNodeData = new JSONObject();
				jsonObject = new JSONObject();
				nodeArray.clear();
				edgeArray.clear();
				jsonNode.put("id", "node" + 0);
				jsonNode.put("label", "label" + 0);
				jsonNode.put("contents", "");
				jsonNode.put("type", "ROOTNODE");
				jsonNode.put("linenum", 1);
				jsonNodeData.put("data", jsonNode);
				nodeArray.add(jsonNodeData);
				jsonNodeId = 0;
				ASTtoJsonPrint(beforeCu, beforeCu.getRoot(), bufWriter, 0, "");
				jsonObject.put("node", nodeArray);
				jsonObject.put("edge", edgeArray);
				bufWriter.close();
				bufWriter = new BufferedWriter(new FileWriter(new File(aFile.getAbsolutePath().substring(0, aFile.getAbsolutePath().length()-4) + "json")));
				bufWriter.write(jsonObject.toJSONString());
				bufWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			

			// Extract additional information from Json using var name and M.I.
			HashMap<String, ArrayList<String>> additionalLeftContext = new HashMap<>();
			HashMap<String, ArrayList<String>> additionalRightContext = new HashMap<>();
			getStatement(leftNameList, additionalLeftContext);
			getStatement(rightNameList, additionalRightContext);

			System.out.println("------ after abstarct -------");
			String tempString = "";
			contextNodeList.clear();
			if (left != null) {
				tempString = getNormalizedStatement(additionalLeftContext, beforeCu, afterFileLine, beforeFileLine, beforeTree);
			}
			c.leftRelatedStatement = tempString.toString();
			
			// TreeNode beforeTreeRootNode = beforeTree.getRoot();
			// for(int i = 0; i< beforeTree.bfs(beforeTreeRootNode).size(); i++) {
			// 	if(contextNodeList.size() != 0 
			// 		&& beforeTree.bfs(beforeTreeRootNode).get(i).getASTNode().toString().equals(contextNodeList.get(0).toString())) {
			// 		System.out.println(beforeTree.bfs(beforeTreeRootNode).get(i).getId());
			// 		System.out.println(beforeTree.bfs(beforeTreeRootNode).get(i).getASTNode());
			// 	}
			// }

			// tempString = "";
			// contextNodeList.clear();
			// if (right != null) {
			// 	tempString = getNormalizedStatement(additionalRightContext, beforeCu, afterFileLine, beforeFileLine);
			// }
			// c.rightRelatedStatement = tempString;

			return new Context(sb.toString());
		}else{
			return getContext(op.getNode());
		}
	}

	private String getNormalizedStatement(HashMap<String, ArrayList<String>> additionalContext, CompilationUnit comUnit, String afterFileString, String beforeFileString, Tree beforeTree) {
		Iterator<String> iter = additionalContext.keySet().iterator();
		StringBuffer sBuffer = new StringBuffer();
		MVTManager manager = new MVTManager();
		while (iter.hasNext()) {
			String keyName = iter.next();
			for (int j = 0; j < additionalContext.get(keyName).size(); j++) {
				findRelatedNode(comUnit, comUnit.getRoot(), 0, "", additionalContext.get(keyName).get(j));
			}
			for (int j = 0; j < contextNodeList.size(); j++) {
				// TreeNode tempTreeNode = new TreeNode(-1, TreeUtils.getLabel(tempASTNode), tempASTNode);
				System.out.println("before normalize: " + contextNodeList.get(j));
				TreeNode tempTreeNode = null;
				for (int k = 0; k < beforeTree.bfs().size(); k++) {
					if (beforeTree.bfs().get(k).getASTNode().toString().equals(contextNodeList.get(j).toString())) {
						System.out.println("right!: " + beforeTree.bfs().get(k).getASTNode());
						tempTreeNode = beforeTree.bfs().get(k);
						break;
					}
				}
				
				// if (tempTreeNode.isMatched()) {
				// 	tempTreeNode = tempTreeNode.getMatched();
				// }
				Node tempNode = Converter.convert(tempTreeNode);
				// System.out.println("Before NODE: " + tempNode);
				manager = new MVTManager();
				TreeUtils.normalize(manager, tempNode, false);
				tempNode.normalized = false;
				System.out.println("after normalize: " + Converter.getNormalizedCode(tempNode, beforeFileString));
				System.out.println("-----------------------");
				sBuffer.append(Converter.getNormalizedCode(tempNode, beforeFileString) + "\n");
			}
		}
		return sBuffer.toString();
		} 

	private void getStatement(ArrayList<String> nameList, HashMap<String, ArrayList<String>> additionalContext) {
		String tempIdChild = "";
		String tempIdParent = "";

		for (int i = 0; i < nameList.size(); i++) {
			ArrayList<String> tempList = new ArrayList<>();
			for (int j = 0; j < nodeArray.size(); j++) {
				JSONObject tempObject = (JSONObject) ((JSONObject) nodeArray.get(j)).get("data");
				if (tempObject.get("type").equals("name")) {
					if (tempObject.get("contents").equals(nameList.get(i))) {
						tempIdChild = tempObject.get("id").toString();
						while(!(tempObject.get("type").equals("expression") || tempObject.get("type").equals("statements")
						|| tempObject.get("type").equals("ROOTNODE") || tempObject.get("type").equals("body"))) {
							for(int k = 0; k < edgeArray.size(); k++) {
								if (((JSONObject) ((JSONObject) edgeArray.get(k)).get("data")).get("target").equals(tempIdChild)) {
									tempIdParent = (String) ((JSONObject)((JSONObject) edgeArray.get(k)).get("data")).get("source");
									break;
								}
							}
							for(int k = 0; k < nodeArray.size(); k++) {
								if (((JSONObject)((JSONObject) nodeArray.get(k)).get("data")).get("id").equals(tempIdParent)) {
									tempObject = (JSONObject) ((JSONObject) nodeArray.get(k)).get("data");
									tempIdChild = ((JSONObject) ((JSONObject) nodeArray.get(k)).get("data")).get("id").toString();
									break;
								}
							}
						}
						if (tempObject.get("type").equals("statements") || tempObject.get("type").equals("expression")) {
							tempList.add(tempObject.get("contents").toString());
						}
					}
				}
			}
			additionalContext.put(nameList.get(i), tempList);
		}
	}

	private void findRelatedNode(CompilationUnit cu, ASTNode node, int parentNodeId, String typeName, String keyName) {
		int thisNodeId = ++jsonNodeId;
		List properties = node.structuralPropertiesForType();

		if (node.toString().equals(keyName)) {
			contextNodeList.add(node);
		}
		for (Iterator iterator = properties.iterator(); iterator.hasNext();) {
			Object descriptor = iterator.next();
			if (descriptor instanceof SimplePropertyDescriptor) {
			} else if (descriptor instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor child = (ChildPropertyDescriptor) descriptor;
				ASTNode childNode = (ASTNode) node.getStructuralProperty(child);
				if (childNode != null) {
					findRelatedNode(cu, childNode, thisNodeId, child.getId(), keyName);
				}
			} else {
				ChildListPropertyDescriptor list = (ChildListPropertyDescriptor) descriptor;
				findRelatedNode(cu, (List) node.getStructuralProperty(list), thisNodeId, list.getId(), keyName);
			}
		}
	}
	
	private void findRelatedNode(CompilationUnit cu, List nodes, int parentNodeId, String typeName,  String keyName) {
		for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
			findRelatedNode(cu, (ASTNode) iterator.next(), parentNodeId, typeName, keyName);
		}
	}

	// TODO: change부분 색칠해서 눈에 띄게 보이기
	private void ASTtoJsonPrint(CompilationUnit cu, ASTNode node, BufferedWriter bufWriter, int parentNodeId, String typeName) throws IOException {
		int thisNodeId = ++jsonNodeId;
		JSONObject jsonEdge = new JSONObject();
		JSONObject jsonNode = new JSONObject();
		JSONObject jsonEdgeData = new JSONObject();
		JSONObject jsonNodeData = new JSONObject();
		// imports, javadoc
		
		jsonEdge.put("source", "node" + parentNodeId);
		jsonEdge.put("target", "node" + thisNodeId);
		jsonEdgeData.put("data", jsonEdge);
		jsonNode.put("id", "node" + thisNodeId);
		jsonNode.put("label", "label" + thisNodeId);
		jsonNode.put("contents", node.toString());
		jsonNode.put("type", typeName);
		jsonNode.put("linenum", cu.getLineNumber(node.getStartPosition()));
		jsonNodeData.put("data", jsonNode);
		if (!(typeName.equals("imports") || typeName.equals("javadoc"))) {
			edgeArray.add(jsonEdgeData);
			nodeArray.add(jsonNodeData);
		} else {
			return;
		}
		
		bufWriter.write("edge:(" + Integer.toString(parentNodeId) + ", " + Integer.toString(thisNodeId) + ")\n");
		bufWriter.write("nodeid: " + Integer.toString(thisNodeId) + "\n");
		bufWriter.write("nodecontents: " + node.toString() + "\n");
		bufWriter.write("nodetype:" + typeName + "\n");
		bufWriter.write("nodelinenum:" + cu.getLineNumber(node.getStartPosition()) + "\n");
		List properties = node.structuralPropertiesForType();
		for (Iterator iterator = properties.iterator(); iterator.hasNext();) {
			Object descriptor = iterator.next();
			if (descriptor instanceof SimplePropertyDescriptor) {
			} else if (descriptor instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor child = (ChildPropertyDescriptor) descriptor;
				ASTNode childNode = (ASTNode) node.getStructuralProperty(child);
				if (childNode != null) {
					bufWriter.write("Child (" + child.getId() + ") {" + "\n");
					ASTtoJsonPrint(cu, childNode, bufWriter, thisNodeId, child.getId());
					bufWriter.write("}\n");
				}
			} else {
				ChildListPropertyDescriptor list = (ChildListPropertyDescriptor) descriptor;
				bufWriter.write("List (" + list.getId() + "){" + "\n");
				ASTtoJsonPrint(cu, (List) node.getStructuralProperty(list), bufWriter, thisNodeId, list.getId());
				bufWriter.write("}\n");
			}
		}
	}
	
	private void ASTtoJsonPrint(CompilationUnit cu, List nodes, BufferedWriter bufWriter, int parentNodeId, String typeName) throws IOException {
		for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
			ASTtoJsonPrint(cu, (ASTNode) iterator.next(), bufWriter, parentNodeId, typeName);
		}
	}

	private void extractNameinContext(TreeNode node, ArrayList<String> nameArrayList) {
		if (node.getLabel().contains("Declaration") || node.getLabel().contains("Assignment")
			|| node.getLabel().contains("Invocation")) {
			for (int i = 0; i < node.children.size(); i++) {
				getNameinContext(node.children.get(i), nameArrayList);
			}
		} else {
			for(int i = 0; i < node.children.size(); i++) {
				extractNameinContext(node.children.get(i), nameArrayList);
			}
		}
	}

	private void getNameinContext(TreeNode node, ArrayList<String> nameArrayList) {
		if (node.getLabel().contains("Name")) {
			nameArrayList.add(node.getASTNode().toString());
		} else {
			for (int i = 0; i < node.children.size(); i++) {
				getNameinContext(node.children.get(i), nameArrayList);
			}
		}
	}

	@Override
	public Context getContext(EditOp op) {
		if(op.getType().equals(Change.INSERT)){
			StringBuffer sb = new StringBuffer();
			sb.append("P:");
			TreeNode parent = null;
			
			parent = op.getNode().getParent();
			if(parent.getType() == ASTNode.BLOCK && parent.getParent() != null)
				parent = parent.getParent();
			
			System.out.println("op: " + op.toOpString());

			if(parent != null){
				if(parent.getMatched() != null){
					parent = parent.getMatched();
				}
				sb.append(TreeUtils.getTypeName(parent.getType()));
				StructuralPropertyDescriptor desc = null;
				ASTNode astNode = op.getNode().getASTNode();
				if(astNode.getParent().getNodeType() == ASTNode.EXPRESSION_STATEMENT)
					desc = astNode.getParent().getLocationInParent();
				else
					desc = astNode.getLocationInParent();
				if(desc != null){
					sb.append(TreeUtils.SYM_OPEN);
					sb.append(desc.getId());
					sb.append(TreeUtils.SYM_CLOSE);
				}
			}
			// System.out.println("parentCode:" + parent.getASTNode());
			TreeNode node = op.getNode();
			TreeNode left = node.getLeft();
			TreeNode right = node.getRight();
			while(right != null && !right.isMatched())
				right = right.getRight();
			//Get hash of old nodes as contexts.
			if(left != null && left.isMatched())
				left = left.getMatched();
			if(right != null && right.isMatched())
				right = right.getMatched();
			sb.append(",L:");
			if(left != null)
				addNodeType(left, sb);
			sb.append(",R:");
			if(right != null)
				addNodeType(right, sb);			
			return new Context(sb.toString());
		}else{
			return getContext(op.getNode());
		}
	}

	private void addNodeType(TreeNode node, StringBuffer sb){
		sb.append(TreeUtils.getTypeName(node.getType()));
		StructuralPropertyDescriptor desc = null;
		ASTNode astNode = node.getASTNode();
		if(astNode.getParent().getNodeType() == ASTNode.EXPRESSION_STATEMENT)
			desc = astNode.getParent().getLocationInParent();
		else
			desc = astNode.getLocationInParent();
		if(desc != null){
			sb.append(TreeUtils.SYM_OPEN);
			sb.append(desc.getId());
			sb.append(TreeUtils.SYM_CLOSE);
		}
	}

	@Override
	public Context getContext(TreeNode node){
		StringBuffer sb = new StringBuffer();
		sb.append("P:");
		TreeNode parent = null;
		if(node.getParent() != null && node.getParent().getType() == ASTNode.BLOCK){
			parent = node.getParent().getParent();
		}else{
			parent = node.getParent();
		}
		if(parent != null){
			sb.append(TreeUtils.getTypeName(parent.getType()));
			StructuralPropertyDescriptor desc = null;
			ASTNode astNode = node.getASTNode();
			if(astNode.getParent().getNodeType() == ASTNode.EXPRESSION_STATEMENT)
				desc = astNode.getParent().getLocationInParent();
			else
				desc = astNode.getLocationInParent();
			if(desc != null){
				sb.append(TreeUtils.SYM_OPEN);
				sb.append(desc.getId());
				sb.append(TreeUtils.SYM_CLOSE);
			}
		}
		TreeNode left = node.getLeft();
		TreeNode right = node.getRight();
		sb.append(",L:");
		if(left != null)
			addNodeType(left, sb);
		sb.append(",R:");
		if(right != null)
			addNodeType(right, sb);
		return new Context(sb.toString());
	}

	@Override
	public Context getContext(Node node) {
		StringBuffer sb = new StringBuffer();
		sb.append("P:");
		Node parent = node.parent != null && node.parent.type == ASTNode.BLOCK ? node.parent.parent : node.parent;
		sb.append(getParentType(node, parent));
		if(parent != null && node.desc != null){
			sb.append(TreeUtils.SYM_OPEN);
			sb.append(node.desc.id);
			sb.append(TreeUtils.SYM_CLOSE);
		}
		Node left = node.getLeft();
		sb.append(",L:");
		if(left != null)
			addNodeType(left, sb);
		sb.append(",R:");
		//Find the first unmatched right node.
		Node right = node.getRight();
		while(right != null && !right.isMatched)
			right = right.getRight();
		if(right != null)
			addNodeType(right, sb);
		return new Context(sb.toString());
	}

	@Override
	public Context getLeftContext(Node node) {
		Node left = node.getLeft();
		StringBuffer sb = new StringBuffer();
		sb.append("P:");
		Node parent = node.parent != null && node.parent.type == ASTNode.BLOCK ? node.parent.parent : node.parent;
		sb.append(getParentType(node, parent));
		if(parent != null && node.desc != null){
			sb.append(TreeUtils.SYM_OPEN);
			if(parent.type == ASTNode.INFIX_EXPRESSION) {
				if(node.desc.id.equals(InfixExpression.RIGHT_OPERAND_PROPERTY.getId()))
					sb.append(InfixExpression.LEFT_OPERAND_PROPERTY.getId());
				else if(node.desc.id.equals(InfixExpression.EXTENDED_OPERANDS_PROPERTY.getId()))
					sb.append(InfixExpression.RIGHT_OPERAND_PROPERTY.getId());
				else
					sb.append(node.desc.id);
			} else if(parent.type == ASTNode.METHOD_INVOCATION
					&& node.desc.id.equals(MethodInvocation.NAME_PROPERTY.getId())) {
				sb.append(MethodInvocation.EXPRESSION_PROPERTY.getId());
			} else {
				sb.append(node.desc.id);
			}
			sb.append(TreeUtils.SYM_CLOSE);
		}
		sb.append(",L:");
		if(left != null)
			addNodeType(left, sb);
		sb.append(",R:");
		addNodeType(node, sb);
		return new Context(sb.toString());
	}

	@Override
	public Context getRightContext(Node node){
		Node right = node.getRight();
		StringBuffer sb = new StringBuffer();
		sb.append("P:");
		Node parent = node.parent != null && node.parent.type == ASTNode.BLOCK ? node.parent.parent : node.parent;
		sb.append(getParentType(node, parent));
		if(parent != null && node.desc != null){
			sb.append(TreeUtils.SYM_OPEN);
			if(parent.type == ASTNode.INFIX_EXPRESSION) {
				if(node.desc.id.equals(InfixExpression.LEFT_OPERAND_PROPERTY.getId()))
					sb.append(InfixExpression.RIGHT_OPERAND_PROPERTY.getId());
				else if(node.desc.id.equals(InfixExpression.RIGHT_OPERAND_PROPERTY.getId()))
					sb.append(InfixExpression.EXTENDED_OPERANDS_PROPERTY.getId());
				else
					sb.append(node.desc.id);
			} else if(parent.type == ASTNode.IF_STATEMENT
					&& node.desc.id.equals(IfStatement.THEN_STATEMENT_PROPERTY.getId())) {
				sb.append(IfStatement.ELSE_STATEMENT_PROPERTY.getId());
			} else {
				sb.append(node.desc.id);
			}
			sb.append(TreeUtils.SYM_CLOSE);
		}
		sb.append(",L:");
		addNodeType(node, sb);
		sb.append(",R:");
		if(right != null)
			addNodeType(right, sb);
		return new Context(sb.toString());
	}
}
