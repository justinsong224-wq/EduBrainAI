# app/services/document_parser.py
import os
from pathlib import Path
from langchain_text_splitters import RecursiveCharacterTextSplitter

class DocumentParser:
    def __init__(self):
        self.splitter = RecursiveCharacterTextSplitter(
            chunk_size=500,
            chunk_overlap=50,
            separators=["\n\n", "\n", "。", "！", "？", ".", "!", "?", " ", ""]
        )

    def parse(self, file_path: str) -> list[str]:
        path = Path(file_path)
        ext = path.suffix.lower()

        if ext == ".pdf":
            text = self._parse_pdf(file_path)
        elif ext in [".txt", ".md"]:
            text = self._parse_text(file_path)
        elif ext == ".docx":
            text = self._parse_docx(file_path)
        else:
            raise ValueError(f"不支持的文件类型: {ext}")

        chunks = self.splitter.split_text(text)
        chunks = [c.strip() for c in chunks if len(c.strip()) > 10]
        return chunks

    def _parse_pdf(self, file_path: str) -> str:
        text = ""

        # 第一步：pdfplumber
        try:
            import pdfplumber
            with pdfplumber.open(file_path) as pdf:
                for page in pdf.pages:
                    page_text = page.extract_text()
                    if page_text:
                        text += page_text + "\n"
            print(f"[PDF] pdfplumber 提取字符数: {len(text)}")
        except Exception as e:
            print(f"[PDF] pdfplumber 失败: {repr(e)}")

        # 第二步：pymupdf
        if not text.strip():
            print("[PDF] pdfplumber 提取为空，尝试 pymupdf...")
            try:
                import fitz
                doc = fitz.open(file_path)
                for page in doc:
                    page_text = page.get_text()
                    if page_text:
                        text += page_text + "\n"
                print(f"[PDF] pymupdf 提取字符数: {len(text)}")
                doc.close()
            except Exception as e:
                print(f"[PDF] pymupdf 失败: {repr(e)}")

        # 第三步：OCR
        if not text.strip():
            print("[PDF] 文字提取为空，尝试 OCR...")
            try:
                import fitz
                doc = fitz.open(file_path)
                for page in doc:
                    pix = page.get_pixmap(dpi=200)
                    ocr_text = page.get_textpage_ocr(flags=0, language="chi_sim+eng")
                    text += page.get_text(textpage=ocr_text) + "\n"
                print(f"[PDF] OCR 提取字符数: {len(text)}")
                doc.close()
            except Exception as e:
                print(f"[PDF] OCR 失败: {repr(e)}")

        if not text.strip():
            raise ValueError("PDF 内容为空，无法解析")

        return text

    def _parse_text(self, file_path: str) -> str:
        with open(file_path, "r", encoding="utf-8") as f:
            return f.read()

    def _parse_docx(self, file_path: str) -> str:
        try:
            from docx import Document
            doc = Document(file_path)
            return "\n".join([para.text for para in doc.paragraphs])
        except ImportError:
            raise ImportError("请安装 python-docx: pip install python-docx")


# 单例
document_parser = DocumentParser()